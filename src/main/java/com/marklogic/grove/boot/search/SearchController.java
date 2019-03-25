package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.grove.boot.AbstractController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/search")
public class SearchController extends AbstractController {

	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public JsonNode search(@PathVariable String type, @RequestBody ObjectNode searchRequest, HttpSession session) {
		long start = 1;
		long pageLength = 10;
		if (searchRequest.has("options")) {
			ObjectNode options = (ObjectNode) searchRequest.get("options");
			if (options.has("start")) {
				start = options.get("start").asLong();
			}
			if (options.has("pageLength")) {
				pageLength = options.get("pageLength").asLong();
			}
		}

		DatabaseClient client = (DatabaseClient) session.getAttribute("grove-spring-boot-client");

		QueryManager mgr = client.newQueryManager();
		mgr.setPageLength(pageLength);
		StructuredQueryBuilder sqb = mgr.newStructuredQueryBuilder(type);
		StructuredQueryDefinition query = buildQuery(sqb, searchRequest.get("filters"));
		return processResults(mgr.search(query, new JacksonHandle(), start).get());
	}

	protected StructuredQueryDefinition buildQuery(StructuredQueryBuilder sqb, JsonNode filters) {
		if (filters.has("and")) {
			JsonNode and = filters.get("and");
			return sqb.and(arrayFromNode(sqb, and));
		}
		else if (filters.has("or")) {
			JsonNode or = filters.get("or");
			return sqb.or(arrayFromNode(sqb, or));
		}
		else if (filters.has("not")) {
			JsonNode not = filters.get("not");
			return sqb.not(buildQuery(sqb, not));
		}
		else if (filters.has("near")) {
			JsonNode near = filters.get("near");
			return sqb.near(arrayFromNode(sqb, near));
		}
		else {
			String type = filters.has("type") ? filters.get("type").asText() : "selection";
			if (type.equals("queryText")) {
				if (filters.has("constraint")) {
					return createConstraint(sqb, filters.get("constraintType").asText(), filters.get("constraint").asText(), "EQ", filters.get("value"));
				}

				String value = filters.get("value").asText();
				if (value.isEmpty()) {
					return sqb.and();
				}
				return sqb.term(value);
			}
			else {
				List<StructuredQueryDefinition> queries = new ArrayList<>();
				if (type.equals("selection")) {
					JsonNode value = filters.get("value");
					if (value.isArray()) {
						for (JsonNode v : value) {
							queries.addAll(createSelectionQueries(sqb, filters.get("constraintType").asText(), filters.get("constraint").asText(), v));
						}
					} else {
						queries.addAll(createSelectionQueries(sqb, filters.get("constraintType").asText(), filters.get("constraint").asText(), value));
					}
				}
				else if (type.equals("range")) {
					filters.get("value").fieldNames().forEachRemaining(key -> {
						String constraintType = filters.has("constraintType") ? filters.get("constraintType").asText() : "";
						queries.add(createConstraint(sqb, constraintType, filters.get("constraint").asText(), key.toUpperCase(), filters.get("value").get(key)));
					});
				}

				if (queries.size() == 1) {
					return queries.get(0);
				}

				String filterMode = filters.has("mode") ? filters.get("mode").asText() : "and";
				if (filterMode.equals("or")) {
					return sqb.or(queries.toArray(new StructuredQueryDefinition[0]));
				}

				return sqb.and(queries.toArray(new StructuredQueryDefinition[0]));
			}
		}
	}

	private StructuredQueryDefinition[] arrayFromNode(StructuredQueryBuilder sqb, JsonNode node) {
		StructuredQueryDefinition[] q;
		if (!node.isArray()) {
			q = new StructuredQueryDefinition[] { buildQuery(sqb, node) };
		}
		else {
			q = StreamSupport.stream(node.spliterator(), false)
					.map(jsonNode -> buildQuery(sqb, jsonNode))
					.toArray(StructuredQueryDefinition[]::new);
		}
		return q;
	}

	private List<StructuredQueryDefinition> createSelectionQueries(StructuredQueryBuilder sqb, String constraintType, String constraint, JsonNode v) {
		List<StructuredQueryDefinition> queries = new ArrayList<>();
		if (v.has("not")) {
			queries.add(sqb.not(createConstraint(sqb, constraintType, constraint, "EQ", v.get("not"))));
		}
		else {
			queries.add(createConstraint(sqb, constraintType, constraint, "EQ", v));
		}
		return queries;
	}

	private StructuredQueryDefinition createConstraint(StructuredQueryBuilder sqb, String constraintType, String constraint, String operator, JsonNode value) {
		switch (constraintType) {
			case "value":
				return sqb.valueConstraint(constraint, value.asText());
			case "word":
				return sqb.wordConstraint(constraint, value.asText());
			case "custom":
				return sqb.customConstraint(constraint, value.asText());
			case "collection":
				return sqb.collectionConstraint(constraint, value.asText());
			case "geospatial":
				if (value.has("north")) {
					return sqb.geospatialConstraint(constraint, sqb.box(value.get("south").asDouble(), value.get("west").asDouble(), value.get("north").asDouble(), value.get("east").asDouble()));
				}
				else if (value.has("latitude")) {
					return sqb.geospatialConstraint(constraint, sqb.point(value.get("latitude").asDouble(), value.get("longitude").asDouble()));
				}
				else if (value.has("radius")) {
					return sqb.geospatialConstraint(constraint, sqb.circle(sqb.point(value.get("point").get("latitude").asDouble(), value.get("point").get("longitude").asDouble()), value.get("radius").asDouble()));
				}
				else if (value.has("point") && value.get("point").isArray()) {
					return sqb.geospatialConstraint(constraint, sqb.polygon(
							StreamSupport.stream(value.get("point").spliterator(), false)
									.map(jsonNode -> sqb.point(jsonNode.get("latitude").asDouble(), jsonNode.get("longitude").asDouble()))
									.toArray(StructuredQueryBuilder.Point[]::new)
					));
				}
			default:
				return sqb.rangeConstraint(constraint, StructuredQueryBuilder.Operator.valueOf(operator), value.asText());
		}
	}

	protected JsonNode processResults(JsonNode node) {
		StreamSupport.stream(node.get("results").spliterator(), false)
				.map(jsonNode -> (ObjectNode)jsonNode)
				.map(jsonNode -> {
					try {
						jsonNode.put("id", URLEncoder.encode(jsonNode.get("uri").asText(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					return jsonNode;
				}).collect(Collectors.toList());
		return node;
	}
}
