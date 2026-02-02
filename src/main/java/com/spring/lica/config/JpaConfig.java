package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "app.module.jpa.enabled", havingValue = "true")
@Import(HibernateJpaAutoConfiguration.class)
@EnableJpaRepositories(basePackages = "com.spring.lica")
public class JpaConfig {

	private static final Logger log = LoggerFactory.getLogger(JpaConfig.class);

	public JpaConfig() {
		log.info("JPA module enabled");
	}
}
