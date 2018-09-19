package com.marklogic.grove.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarkLogicConfig {

	@Value("${mlHost:localhost}")
	private String host;

	@Value("${mlRestPort}")
	private Integer restPort;

	public String getHost() {
		return host;
	}

	public Integer getRestPort() {
		return restPort;
	}
}
