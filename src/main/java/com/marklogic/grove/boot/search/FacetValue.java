package com.marklogic.grove.boot.search;

public class FacetValue {

	private String label;
	private String name;
	private long count; // using long because that's what the DatabaseClient uses
	private Object value; // string or number of boolean

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
