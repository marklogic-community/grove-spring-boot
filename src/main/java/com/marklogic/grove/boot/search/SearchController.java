package com.marklogic.grove.boot.search;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.FacetResult;
import com.marklogic.client.query.FacetValue;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.grove.boot.AbstractController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController extends AbstractController {

	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public SearchFacetsAndResults search(@PathVariable String type, @RequestBody SearchRequest searchRequest, HttpSession session) {
		final long start = searchRequest.getOptions().getStart();
		final long pageLength = searchRequest.getOptions().getPageLength();

		DatabaseClient client = (DatabaseClient) session.getAttribute("grove-spring-boot-client");

		QueryManager mgr = client.newQueryManager();
		mgr.setPageLength(pageLength);
		QueryDefinition query = mgr.newStringDefinition(type);
		SearchHandle handle = mgr.search(query, new SearchHandle(), start);
		return convertToSearchFacetsAndResults(handle);
	}

	protected SearchFacetsAndResults convertToSearchFacetsAndResults(SearchHandle handle) {
		SearchFacetsAndResults facetsAndResults = new SearchFacetsAndResults();
		facetsAndResults.setPageLength(handle.getPageLength());
		facetsAndResults.setStart(handle.getStart());
		facetsAndResults.setTotal(handle.getTotalResults());

		SearchResult[] results = new SearchResult[handle.getMatchResults().length];
		for (int i = 0; i < results.length; i++) {
			results[i] = convertToSearchResult(handle.getMatchResults()[i]);
		}
		facetsAndResults.setResults(results);

		FacetResult[] facetResults = handle.getFacetResults();
		SearchFacets searchFacets = new SearchFacets();
		List<SearchFacet> facetList = new ArrayList<>();
		if (facetResults != null) {
			for (FacetResult facetResult : facetResults) {
				SearchFacet searchFacet = new SearchFacet();
				// TODO This doesn't seem correct, but the name has to go somewhere right??
				searchFacet.setType(facetResult.getName());
				List<com.marklogic.grove.boot.search.FacetValue> groveFacetValues = new ArrayList<>();
				for (FacetValue facetValue : facetResult.getFacetValues()) {
					com.marklogic.grove.boot.search.FacetValue groveFacetValue = new com.marklogic.grove.boot.search.FacetValue();
					groveFacetValue.setName(facetValue.getName());
					groveFacetValue.setLabel(facetValue.getLabel());
					groveFacetValue.setCount(facetValue.getCount());
					groveFacetValue.setValue(facetValue.getName()); // name = value
					groveFacetValues.add(groveFacetValue);
				}
				searchFacet.setFacetValues(groveFacetValues.toArray(new com.marklogic.grove.boot.search.FacetValue[]{}));
				facetList.add(searchFacet);
			}
		}
		searchFacets.setFacets(facetList.toArray(new SearchFacet[]{}));
		facetsAndResults.setFacets(searchFacets);

		return facetsAndResults;
	}

	protected SearchResult convertToSearchResult(MatchDocumentSummary match) {
		SearchResult result = new SearchResult();
		result.setId(match.getUri()); // TODO Not sure how an ID differs from a URI
		result.setUri(match.getUri());
		result.setLabel(match.getPath()); // TODO Not sure how to determine a label yet
		result.setScore(match.getScore());
		return result;
	}
}
