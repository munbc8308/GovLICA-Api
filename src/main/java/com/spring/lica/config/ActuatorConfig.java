package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.module.actuator.enabled", havingValue = "true")
public class ActuatorConfig {

	private static final Logger log = LoggerFactory.getLogger(ActuatorConfig.class);

	public ActuatorConfig() {
		log.info("Actuator module enabled");
	}
}
