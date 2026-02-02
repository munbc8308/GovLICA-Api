package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.graphql.enabled", havingValue = "true")
@Import({GraphQlAutoConfiguration.class, GraphQlWebMvcAutoConfiguration.class})
public class GraphQlConfig {

	private static final Logger log = LoggerFactory.getLogger(GraphQlConfig.class);

	public GraphQlConfig() {
		log.info("GraphQL module enabled");
	}
}
