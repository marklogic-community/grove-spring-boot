package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.impl.QueryManagerImpl;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xmlunit.assertj.XmlAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
class SearchControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private ClassLoader loader = this.getClass().getClassLoader();

    private QueryManagerImpl queryManager = new QueryManagerImpl(null);

    private StructuredQueryBuilder sqb = queryManager.newStructuredQueryBuilder();

    private StructuredQueryDefinition queryDefinition;

    @Autowired
    private SearchController controller;

    @Test
    void testEndpoint() {
        // TODO: test the endpoint with pagination
    }

    @Test
    void emptyQuery() {
        verify("empty-querytext.json", "empty-querytext.xml");
    }

    @Test
    void buildSelectionAndQuery() {
        verify("selection-and.json", "selection-and-query.xml");

        assertEquals("test", queryDefinition.getCriteria(), "If a queryText filter is included without a constraint, " +
            "then the value of that queryText filter should be used to set the criteria on the overall QueryDefinition. " +
            "Note that if multiple such queryText filters are included, only the last one will take effect, since only " +
            "criteria string can be set.");
    }

    @Test
    void buildSelectionAndQueryNonArray() {
        verify("selection-and-non-array.json", "selection-and-query-non-array.xml");
    }


    @Test
    void buildSelectionOrQuery() {
        verify("selection-or.json", "selection-or-query.xml");
    }

    @Test
    void buildSelectionNotQuery() {
        verify("selection-not.json", "selection-not-query.xml");
    }

    @Test
    void buildSelectionNearQuery() {
        verify("selection-near.json", "selection-near-query.xml");
    }

    @Test
    void buildSelectionConstraintQuery() {
        verify("selection-constraints.json", "selection-constraint.xml");
    }

    @Test
    void buildRangeAndQuery() {
        verify("range-and.json", "range-and-query.xml");
    }

    @Test
    void buildGeoQuery() {
        verify("geosearch.json", "geo-query.xml");
    }

    private void verify(String inputFilename, String outputFilename) {
        assertThat(
            getOutput(outputFilename))
            .and(Input.fromString(buildQuery(inputFilename).serialize()).build())
            .ignoreWhitespace()
            .areIdentical();
    }

    private Source getOutput(String outputFilename) {
        return Input.fromFile(loader.getResource("search-controller/output/" + outputFilename).getFile()).build();
    }

    private StructuredQueryDefinition buildQuery(String inputFilename) {
        try {
            JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/" + inputFilename));
            queryDefinition = controller.buildQueryWithCriteria(sqb, query.get("filters"));
            return queryDefinition;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}