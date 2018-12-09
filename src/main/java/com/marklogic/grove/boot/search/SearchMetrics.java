package com.marklogic.grove.boot.search;

public class SearchMetrics {

	private String totalTime;

	public SearchMetrics() {
	}

	public SearchMetrics(String totalTime) {
		this.totalTime = totalTime;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
}
