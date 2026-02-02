package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "app.module.mail.enabled", havingValue = "true")
@Import(MailSenderAutoConfiguration.class)
public class MailConfig {

	private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

	public MailConfig() {
		log.info("Mail module enabled");
	}
}
