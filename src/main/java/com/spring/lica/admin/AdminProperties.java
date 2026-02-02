package com.spring.lica.admin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {
	private String username = "admin";
	private String password = "admin";
	private String settingsFile = "src/main/resources/application.properties";
}
