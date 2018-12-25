package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.grove.boot.AbstractController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController extends AbstractController {

	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public JsonNode search(@PathVariable String type, @RequestBody ObjectNode searchRequest, HttpSession session) {
		return search(type, searchRequest, session, QueryManager.QueryView.DEFAULT);
	}

	@RequestMapping(value = "/{type}/facets", method = RequestMethod.POST)
	public JsonNode searchForFacets(@PathVariable String type, @RequestBody ObjectNode searchRequest, HttpSession session) {
		return search(type, searchRequest, session, QueryManager.QueryView.FACETS);
	}

	@RequestMapping(value = "/{type}/results", method = RequestMethod.POST)
	public JsonNode searchForResults(@PathVariable String type, @RequestBody ObjectNode searchRequest, HttpSession session) {
		return search(type, searchRequest, session, QueryManager.QueryView.RESULTS);
	}

	/**
	 * The main work here is in converting the JSON search request into a StructuredQueryDefinition via a
	 * StructuredQueryBuilder. This work is done via a protected method so that it can be unit tested.
	 *
	 * @param type
	 * @param searchRequest
	 * @param session
	 * @return
	 */
	private JsonNode search(String type, ObjectNode searchRequest, HttpSession session, QueryManager.QueryView queryView) {
		DatabaseClient client = (DatabaseClient) session.getAttribute("grove-spring-boot-client");

		long start = 1;
		long pageLength = 10;
		if (searchRequest.has("options")) {
			ObjectNode options = (ObjectNode) searchRequest.get("options");
			if (options.has("start")) {
				start = options.get("start").asLong();
			}
			if (options.has("pageLength")) {
				pageLength = options.get("pageLenth").asLong();
			}
		}


		QueryManager mgr = client.newQueryManager();
		mgr.setPageLength(pageLength);
		mgr.setView(queryView);

		StructuredQueryDefinition query = buildStructuredQuery(mgr, type, (ObjectNode) searchRequest.get("filters"));
		return mgr.search(query, new JacksonHandle(), start).get();
	}

	/**
	 * Handles all the work of converting the given filters into a structured query.
	 *
	 * @param mgr
	 * @param type
	 * @param filters
	 * @return
	 */
	protected StructuredQueryDefinition buildStructuredQuery(QueryManager mgr, String type, ObjectNode filters) {
		final String criteria = extractCriteria(filters);
		StructuredQueryDefinition query = processObject(mgr.newStructuredQueryBuilder(type), filters);
		return criteria != null ? query.withCriteria(criteria) : query;
	}

	/**
	 * The search text that a user enters into a search bar is expected to be in the array of the top-level "and" query.
	 * This needs to be removed from the filters object so it can be set on the StructuredQueryDefinition as criteria.
	 *
	 * @param filters
	 * @return
	 */
	private String extractCriteria(ObjectNode filters) {
		if (filters.has("and")) {
			ArrayNode topLevelAndNode = (ArrayNode) filters.get("and");
			for (int i = 0; i < topLevelAndNode.size(); i++) {
				JsonNode node = topLevelAndNode.get(i);
				if (node.has("type") && "queryText".equals(node.get("type").asText()) && node.has("value")) {
					ObjectNode queryTextNode = (ObjectNode) node;
					String value = queryTextNode.get("value").asText();
					if (StringUtils.hasText(value)) {
						topLevelAndNode.remove(i);
						return value;
					}
				}
			}
		}
		return null;
	}

	/**
	 * TODO The example has an "or" query on an array of two strings - but the schema doesn't seem to allow this.
	 *
	 * @param queryBuilder
	 * @param node
	 * @return
	 */
	private StructuredQueryDefinition processObject(StructuredQueryBuilder queryBuilder, ObjectNode node) {
		// Should only have a single field
		String name = node.fieldNames().next();
		if (node.has("type")) {
			return processTypedFilter(queryBuilder, node);
		} else if (node.has("or")) {
			return queryBuilder.or(processArray(queryBuilder, (ArrayNode) node.get("or")));
		} else if (node.has("and")) {
			return queryBuilder.and(processArray(queryBuilder, (ArrayNode) node.get("and")));
		} else if (node.has("not")) {
			return queryBuilder.not(processObject(queryBuilder, (ObjectNode) node.get("not")));
		} else if (node.has("near")) {
			return processNearFilter(queryBuilder, node);
		} else {
			throw new UnsupportedOperationException("Unsupported filter: " + name);
		}
	}

	private StructuredQueryDefinition[] processArray(StructuredQueryBuilder queryBuilder, ArrayNode array) {
		List<StructuredQueryDefinition> queries = new ArrayList<>();
		array.forEach(node -> {
			StructuredQueryDefinition query = processObject(queryBuilder, (ObjectNode) node);
			if (query != null) {
				queries.add(query);
			}
		});
		return queries.toArray(new StructuredQueryDefinition[]{});
	}

	private StructuredQueryDefinition processTypedFilter(StructuredQueryBuilder queryBuilder, JsonNode node) {
		String type = node.get("type").asText().toLowerCase();
		if (type.equals("querytext")) {
			return processQueryTextFilter(queryBuilder, node);
		} else if (type.equals("selection")) {
			return processSelectionFilter(queryBuilder, node);
		} else if (type.equals("range")) {
			return processRangeFilter(queryBuilder, node);
		} else {
			throw new UnsupportedOperationException("Unsupported typed filter type: " + type);
		}
	}

	private StructuredQueryDefinition processQueryTextFilter(StructuredQueryBuilder queryBuilder, JsonNode node) {
		String text = node.get("value").asText();
		return StringUtils.hasText(text) ? queryBuilder.term(text) : null;
	}

	private StructuredQueryDefinition processSelectionFilter(StructuredQueryBuilder queryBuilder, JsonNode node) {
		JsonNode value = node.get("value");
		final String constraintType = node.has("constraintType") ? node.get("constraintType").asText() : "word";
		final String constraint = node.get("constraint").asText();
		if (value instanceof ArrayNode) {
			String mode = "and";
			if (node.has("mode")) {
				mode = node.get("mode").asText();
			}

			List<StructuredQueryDefinition> selectionQueries = new ArrayList<>();
			value.forEach(valueNode -> {
				selectionQueries.add(buildSelectionQuery(queryBuilder, constraint, constraintType, valueNode));
			});
			if ("or".equalsIgnoreCase(mode)) {
				return queryBuilder.or(selectionQueries.toArray(new StructuredQueryDefinition[]{}));
			} else {
				return queryBuilder.and(selectionQueries.toArray(new StructuredQueryDefinition[]{}));
			}
		} else {
			return buildSelectionQuery(queryBuilder, constraint, constraintType, value);
		}
	}

	private StructuredQueryDefinition buildSelectionQuery(StructuredQueryBuilder queryBuilder, String constraint, String constraintType, JsonNode valueNode) {
		String constraintValue = parseValue(valueNode);
		if ("range".equals(constraintType)) {
			return queryBuilder.rangeConstraint(constraint, StructuredQueryBuilder.Operator.EQ, constraintValue);
		}
		return queryBuilder.wordConstraint(constraint, constraintValue);
	}

	private StructuredQueryDefinition processRangeFilter(StructuredQueryBuilder queryBuilder, JsonNode node) {
		ObjectNode value = (ObjectNode) node.get("value");
		final String constraint = node.get("constraint").asText();
		List<StructuredQueryDefinition> rangeQueries = new ArrayList<>();
		value.fieldNames().forEachRemaining(operator -> {
			rangeQueries.add(queryBuilder.rangeConstraint(constraint, StructuredQueryBuilder.Operator.valueOf(operator.toUpperCase()), value.get(operator).asText()));
		});
		return queryBuilder.and(rangeQueries.toArray(new StructuredQueryDefinition[]{}));
	}

	private String parseValue(JsonNode node) {
		if (node.has("not")) {
			return "-" + node.get("not").asText();
		}
		return node.asText();
	}

	/**
	 * TODO The docs don't match the example from search-api.json.
	 *
	 * @param queryBuilder
	 * @param node
	 * @return
	 */
	private StructuredQueryDefinition processNearFilter(StructuredQueryBuilder queryBuilder, ObjectNode node) {
		ObjectNode nearNode = (ObjectNode) node.get("near");
		int distance = nearNode.get("distance").asInt();
		StructuredQueryDefinition leftQuery = processObject(queryBuilder, (ObjectNode) nearNode.get("left"));
		StructuredQueryDefinition rightQuery = processObject(queryBuilder, (ObjectNode) nearNode.get("right"));
		// TODO What should the Ordering be?
		return queryBuilder.near(distance, 0, StructuredQueryBuilder.Ordering.UNORDERED, leftQuery, rightQuery);
	}
}
