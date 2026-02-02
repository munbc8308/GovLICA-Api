package com.spring.lica.messaging.rabbitmq;

import com.spring.lica.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.module.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConsumerService {

	private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumerService.class);

	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void consume(String message) {
		log.info("Received RabbitMQ message: {}", message);
	}
}
