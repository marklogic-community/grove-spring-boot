package com.marklogic.grove.boot.search;

import com.fasterxml.jackson.databind.JsonNode;

public class SearchRequest {

	private JsonNode filters;
	private Options options = new Options();

	public Options getOptions() {
		return options;
	}
	public JsonNode getFilters() {
		return filters;
	}

	public void setOptions(Options options) {
		this.options = options;
	}
}
