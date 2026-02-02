package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.flyway.enabled", havingValue = "true")
@Import(FlywayAutoConfiguration.class)
public class FlywayConfig {

	private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

	public FlywayConfig() {
		log.info("Flyway module enabled");
	}
}
