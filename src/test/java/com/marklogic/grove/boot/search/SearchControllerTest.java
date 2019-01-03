package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.impl.QueryManagerImpl;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import javax.xml.transform.Source;
import java.io.IOException;

import static org.xmlunit.assertj.XmlAssert.assertThat;

class SearchControllerTest {

    private SearchController controller = new SearchController();
    private ObjectMapper objectMapper = new ObjectMapper();

    private ClassLoader loader = this.getClass().getClassLoader();

    private QueryManagerImpl queryManager = new QueryManagerImpl(null);

    private StructuredQueryBuilder sqb = queryManager.newStructuredQueryBuilder();


    @Test
    void buildSelectionAndQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-and.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-and-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
            .and(actual)
            .ignoreWhitespace()
            .areIdentical();
    }

    @Test
    void buildSelectionAndQueryNonArray() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-and-non-array.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-and-query-non-array.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }


    @Test
    void buildSelectionOrQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-or.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-or-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void buildSelectionNotQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-not.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-not-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void buildSelectionNearQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-near.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-near-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void buildSelectionConstraintQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/selection-constraints.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/selection-constraint.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void buildRangeAndQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/range-and.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/range-and-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    void buildGeoQuery() throws IOException {
        JsonNode query = objectMapper.readTree(loader.getResourceAsStream("search-controller/input/geosearch.json"));
        StructuredQueryDefinition q = controller.buildQuery(sqb, query.get("filters"));

        Source expected = Input.fromFile(loader.getResource("search-controller/output/geo-query.xml").getFile()).build();
        Source actual = Input.fromString(q.serialize()).build();
        assertThat(expected)
                .and(actual)
                .ignoreWhitespace()
                .areIdentical();
    }
}