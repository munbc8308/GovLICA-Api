package com.spring.lica.messaging.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.module.kafka.enabled", havingValue = "true")
public class KafkaConsumerService {

	private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

	@KafkaListener(topics = "${app.kafka.topic:lica-topic}", groupId = "${spring.kafka.consumer.group-id}")
	public void consume(String message) {
		log.info("Received Kafka message: {}", message);
	}
}
