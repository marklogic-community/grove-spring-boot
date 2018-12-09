package com.marklogic.grove.boot.search;

public class SearchFacets {

	private SearchFacet[] facets;
	private SearchMetrics metrics;

	public SearchFacet[] getFacets() {
		return facets;
	}

	public void setFacets(SearchFacet[] facets) {
		this.facets = facets;
	}

	public SearchMetrics getMetrics() {
		return metrics;
	}

	public void setMetrics(SearchMetrics metrics) {
		this.metrics = metrics;
	}
}
