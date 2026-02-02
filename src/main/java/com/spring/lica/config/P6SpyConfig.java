package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.module.p6spy.enabled", havingValue = "true")
public class P6SpyConfig {

	private static final Logger log = LoggerFactory.getLogger(P6SpyConfig.class);

	public P6SpyConfig() {
		log.info("P6Spy module enabled");
	}
}
