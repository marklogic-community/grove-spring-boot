package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.query.QueryDefinition;
import com.marklogic.client.query.QueryManager;
import com.marklogic.grove.boot.AbstractController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/search")
public class SearchController extends AbstractController {

	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public JsonNode search(@PathVariable String type, @RequestBody SearchRequest searchRequest, HttpSession session) {
		DatabaseClient client = (DatabaseClient) session.getAttribute("grove-spring-boot-client");

		final long start = searchRequest.getOptions().getStart();
		final long pageLength = searchRequest.getOptions().getPageLength();

		QueryManager mgr = client.newQueryManager();
		mgr.setPageLength(pageLength);
		QueryDefinition query = mgr.newStringDefinition(type);
		return mgr.search(query, new JacksonHandle(), start).get();
	}
}
