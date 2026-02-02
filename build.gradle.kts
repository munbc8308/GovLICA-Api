plugins {
	java
	id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.spring"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("com.h2database:h2")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	// MapStruct
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	// QueryDSL
	implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")

	// Configuration Processor
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// Actuator
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// OpenAPI (Swagger)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	// Flyway
	implementation("org.flywaydb:flyway-core")

	// Mail
	implementation("org.springframework.boot:spring-boot-starter-mail")

	// AOP
	implementation("org.springframework.boot:spring-boot-starter-aop")

	// WebSocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	// OAuth2 Resource Server
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// P6Spy
	implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.2")

	// Thymeleaf
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

	// GraphQL
	implementation("org.springframework.boot:spring-boot-starter-graphql")

	// Config Reload (Spring Cloud Context)
	implementation("org.springframework.cloud:spring-cloud-context")

	// DevTools
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.session:spring-session-data-redis")

	// Kafka
	implementation("org.springframework.kafka:spring-kafka")

	// RabbitMQ
	implementation("org.springframework.boot:spring-boot-starter-amqp")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
