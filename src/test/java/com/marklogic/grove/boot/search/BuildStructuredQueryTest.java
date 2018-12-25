package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.junit5.MarkLogicNamespaceProvider;
import com.marklogic.junit5.XmlNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuildStructuredQueryTest {

	private SearchController controller = new SearchController();
	private DatabaseClient client;

	/**
	 * A DatabaseClient is needed for this test so that a StructuredQueryBuiler can be created, but it does not
	 * need to connect to a MarkLogic host.
	 */
	@BeforeEach
	public void setup() {
		client = DatabaseClientFactory.newClient("localhost", 8000, new DatabaseClientFactory.DigestAuthContext("doesnt", "matter"));
	}

	@AfterEach
	public void tearDown() {
		if (client != null) {
			client.release();
		}
	}

	@Test
	public void exampleFromGroveDocs() throws Exception {
		ObjectNode filters = (ObjectNode) new ObjectMapper().readTree(new FileReader("src/test/resources/search-request.json"));

		StructuredQueryDefinition query = controller.buildStructuredQuery(client.newQueryManager(), "all", filters);
		assertEquals("foo AND bar", query.getCriteria(),
			"The first queryText filter in the top 'and' filter should have been used as the search expression");

		XmlNode xml = toXml(query);
		xml.prettyPrint();

		final String root = "/search:query/search:and-query";
		xml.assertElementExists(root + "/search:and-query[search:word-constraint-query/search:text = 'Geert' " +
			"and search:word-constraint-query/search:text = '-Patrick']");

		xml.assertElementExists(root + "/search:word-constraint-query[search:constraint-name = 'active' and search:text = 'true']");

		xml.assertElementExists(root + "/search:and-query[search:range-constraint-query[search:value = '20' and search:range-operator = 'GE'] " +
			"and search:range-constraint-query[search:value = '99' and search:range-operator = 'NE']]");

		xml.assertElementExists("When constraintType is 'range', should use a range-constraint-query",
			root + "/search:or-query[search:range-constraint-query[search:value = 'blue' and search:range-operator = 'EQ'] " +
				"and search:range-constraint-query[search:value = 'brown' and search:range-operator = 'EQ']]");

		xml.assertElementExists(
			root + "/search:or-query/search:and-query/search:not-query/search:or-query/search:word-constraint-query[" +
				"search:constraint-name = 'occupationCategory' and search:text = 'marketing']");

		xml.assertElementExists(root + "/search:near-query[search:term-query[1]/search:text = 'Patrick' and " +
			"search:term-query[2]/search:text = 'McElwee' and search:ordered = 'false' and search:distance = '10' and search:distance-weight = '0.0']");
	}

//	@Test
//	public void selectionFilterWithBooleanValue() throws Exception {
//		String json = "{\"and\":[{\"type\": \"selection\", \"constraint\": \"active\", \"value\": true}]}";
//		ObjectNode filters = (ObjectNode) new ObjectMapper().readTree(json);
//
//		StructuredQueryDefinition query = controller.buildStructuredQuery(client.newQueryManager(), "all", filters);
//		assertNull(query.getCriteria());
//
//		XmlNode xml = toXml(query);
//		xml.prettyPrint();
//	}

	private XmlNode toXml(StructuredQueryDefinition query) {
		return new XmlNode(query.serialize(), new MarkLogicNamespaceProvider().getNamespaces());
	}

}
