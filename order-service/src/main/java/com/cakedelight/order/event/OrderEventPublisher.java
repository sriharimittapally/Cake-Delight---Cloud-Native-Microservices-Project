
package com.cakedelight.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.cakedelight.order.config.RabbitMQConfig;

@Component
public class OrderEventPublisher {

	private static final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	/**
	 * Initializes the publisher with RabbitTemplate.
	 */
	public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	/**
	 * Publishes an order-completed event to RabbitMQ.
	 */
	public void publishOrderCompletedEvent(OrderCompletedEvent event) {

		logger.info("Publishing order completion event. Order ID: {}", event.getOrderId());

		rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_ROUTING_KEY, event);

		logger.info("Order completion event published. Order ID: {}", event.getOrderId());
	}
}
