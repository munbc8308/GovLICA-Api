package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.thymeleaf.enabled", havingValue = "true")
@Import(ThymeleafAutoConfiguration.class)
public class ThymeleafConfig {

	private static final Logger log = LoggerFactory.getLogger(ThymeleafConfig.class);

	public ThymeleafConfig() {
		log.info("Thymeleaf module enabled");
	}
}
