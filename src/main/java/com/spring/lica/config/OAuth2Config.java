package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.oauth2.enabled", havingValue = "true")
@Import(OAuth2ResourceServerAutoConfiguration.class)
public class OAuth2Config {

	private static final Logger log = LoggerFactory.getLogger(OAuth2Config.class);

	public OAuth2Config() {
		log.info("OAuth2 Resource Server module enabled");
	}
}
