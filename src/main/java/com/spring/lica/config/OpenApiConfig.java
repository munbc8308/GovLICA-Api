package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.module.openapi.enabled", havingValue = "true")
public class OpenApiConfig {

	private static final Logger log = LoggerFactory.getLogger(OpenApiConfig.class);

	public OpenApiConfig() {
		log.info("OpenAPI (Swagger) module enabled");
	}
}
