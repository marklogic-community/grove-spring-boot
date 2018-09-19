package com.marklogic.grove.boot.search;

public class SearchFacetsAndResults {

	private long total;
	private long start;
	private int pageLength;
	private SearchResult[] results;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public int getPageLength() {
		return pageLength;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public SearchResult[] getResults() {
		return results;
	}

	public void setResults(SearchResult[] results) {
		this.results = results;
	}
}
