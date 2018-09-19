package com.marklogic.grove.boot.search;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.grove.boot.AbstractController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/search")
public class SearchController extends AbstractController {

	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public SearchFacetsAndResults search(@PathVariable String type, @RequestBody SearchRequest searchRequest, HttpSession session) {
		DatabaseClient client = (DatabaseClient) session.getAttribute("grove-spring-boot-client");

		final long start = searchRequest.getOptions().getStart();
		final long pageLength = searchRequest.getOptions().getPageLength();

		QueryManager mgr = client.newQueryManager();
		mgr.setPageLength(pageLength);
		QueryDefinition query = mgr.newStringDefinition(type);
		SearchHandle handle = mgr.search(query, new SearchHandle(), start);

		SearchFacetsAndResults facetsAndResults = new SearchFacetsAndResults();
		facetsAndResults.setPageLength(handle.getPageLength());
		facetsAndResults.setStart(handle.getStart());
		facetsAndResults.setTotal(handle.getTotalResults());

		SearchResult[] results = new SearchResult[handle.getMatchResults().length];
		for (int i = 0; i < results.length; i++) {
			MatchDocumentSummary match = handle.getMatchResults()[i];
			SearchResult result = new SearchResult();
			result.setId(match.getUri()); // TODO Not sure how an ID differs from a URI
			result.setUri(match.getUri());
			result.setLabel(match.getPath()); // TODO Not sure how to determine a label yet
			result.setScore(match.getScore());
			results[i] = result;
		}
		facetsAndResults.setResults(results);
		return facetsAndResults;
	}
}
