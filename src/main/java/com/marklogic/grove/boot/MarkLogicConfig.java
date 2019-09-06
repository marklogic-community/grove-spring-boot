package com.marklogic.grove.boot;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarkLogicConfig implements InitializingBean {

	@Value("${mlHost:localhost}")
	private String host;

	@Value("${mlRestPort}")
	private Integer restPort;

	@Value("${mlSimpleSsl}")
	private boolean simpleSsl;

	@Override
	public void afterPropertiesSet() {
		LoggerFactory.getLogger(getClass()).info(
			String.format("Will connect to MarkLogic at %s:%d", host, restPort));
	}

	public String getHost() {
		return host;
	}

	public Integer getRestPort() {
		return restPort;
	}

	public boolean getSimpleSsl() {
		return simpleSsl;
	}
}
