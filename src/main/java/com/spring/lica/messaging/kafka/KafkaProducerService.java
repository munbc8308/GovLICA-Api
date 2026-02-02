package com.spring.lica.messaging.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.module.kafka.enabled", havingValue = "true")
public class KafkaProducerService {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void send(String topic, String message) {
		log.info("Sending Kafka message to topic '{}': {}", topic, message);
		kafkaTemplate.send(topic, message);
	}

	public void send(String topic, String key, String message) {
		log.info("Sending Kafka message to topic '{}' with key '{}': {}", topic, key, message);
		kafkaTemplate.send(topic, key, message);
	}
}
