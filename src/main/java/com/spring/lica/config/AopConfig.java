package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.aop.enabled", havingValue = "true")
@Import(AopAutoConfiguration.class)
public class AopConfig {

	private static final Logger log = LoggerFactory.getLogger(AopConfig.class);

	public AopConfig() {
		log.info("AOP module enabled");
	}
}
