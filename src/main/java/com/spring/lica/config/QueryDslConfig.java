package com.spring.lica.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.module.querydsl.enabled", havingValue = "true")
public class QueryDslConfig {

	private static final Logger log = LoggerFactory.getLogger(QueryDslConfig.class);

	public QueryDslConfig() {
		log.info("QueryDSL module enabled");
	}

	@Bean
	public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
		return new JPAQueryFactory(entityManager);
	}
}
