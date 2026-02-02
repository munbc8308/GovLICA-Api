package com.spring.lica.messaging.rabbitmq;

import com.spring.lica.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.module.rabbitmq.enabled", havingValue = "true")
public class RabbitMQProducerService {

	private static final Logger log = LoggerFactory.getLogger(RabbitMQProducerService.class);

	private final RabbitTemplate rabbitTemplate;

	public RabbitMQProducerService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void send(String message) {
		log.info("Sending RabbitMQ message: {}", message);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "lica.routing.default", message);
	}

	public void send(String routingKey, String message) {
		log.info("Sending RabbitMQ message with routing key '{}': {}", routingKey, message);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
	}
}
