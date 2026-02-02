package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.websocket.enabled", havingValue = "true")
@Import(WebSocketServletAutoConfiguration.class)
public class WebSocketConfig {

	private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

	public WebSocketConfig() {
		log.info("WebSocket module enabled");
	}
}
