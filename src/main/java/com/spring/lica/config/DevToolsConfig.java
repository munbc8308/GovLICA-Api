package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.module.devtools.enabled", havingValue = "true")
public class DevToolsConfig {

	private static final Logger log = LoggerFactory.getLogger(DevToolsConfig.class);

	public DevToolsConfig() {
		log.info("DevTools module enabled");
	}
}
