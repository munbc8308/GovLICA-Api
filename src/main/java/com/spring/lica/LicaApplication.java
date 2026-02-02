package com.spring.lica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;

@SpringBootApplication(exclude = {
	// JDBC
	DataSourceAutoConfiguration.class,
	DataSourceTransactionManagerAutoConfiguration.class,
	JdbcTemplateAutoConfiguration.class,
	H2ConsoleAutoConfiguration.class,
	// JPA
	HibernateJpaAutoConfiguration.class,
	// Flyway
	FlywayAutoConfiguration.class,
	// Mail
	MailSenderAutoConfiguration.class,
	// AOP
	AopAutoConfiguration.class,
	// Thymeleaf
	ThymeleafAutoConfiguration.class,
	// GraphQL
	GraphQlAutoConfiguration.class,
	GraphQlWebMvcAutoConfiguration.class,
	// WebSocket
	WebSocketServletAutoConfiguration.class,
	// OAuth2 Resource Server
	OAuth2ResourceServerAutoConfiguration.class,
	// Redis
	RedisAutoConfiguration.class,
	RedisRepositoriesAutoConfiguration.class,
	SessionAutoConfiguration.class,
	// Kafka
	KafkaAutoConfiguration.class,
	// RabbitMQ
	RabbitAutoConfiguration.class
})
public class LicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(LicaApplication.class, args);
	}

}
