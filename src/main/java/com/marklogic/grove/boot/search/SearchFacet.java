package com.marklogic.grove.boot.search;

public class SearchFacet {

	private String type;
	private FacetValue[] facetValues;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public FacetValue[] getFacetValues() {
		return facetValues;
	}

	public void setFacetValues(FacetValue[] facetValues) {
		this.facetValues = facetValues;
	}
}
