
package com.cakedelight.rating.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrderServiceClient {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceClient.class);

	private final RestClient restClient;

	/**
	 * Initializes the RestClient with the Order Service base URL.
	 */
	public OrderServiceClient(@Value("${order.service.url}") String orderServiceUrl) {

		this.restClient = RestClient.builder().baseUrl(orderServiceUrl).build();
	}

	/**
	 * Verifies whether a customer purchased a specific cake in an order.
	 */
	public boolean verifyCakePurchase(Long orderId, String email, Long cakeId) {

		logger.debug("Verifying cake purchase for order ID: {} and cake ID: {}", orderId, cakeId);

		Boolean response = restClient
				.get().uri(uriBuilder -> uriBuilder.path("/api/orders/{orderId}/verify-purchase")
						.queryParam("email", email).queryParam("cakeId", cakeId).build(orderId))
				.retrieve().body(Boolean.class);

		boolean verified = Boolean.TRUE.equals(response);

		logger.debug("Cake purchase verification result for order ID {}: {}", orderId, verified);

		return verified;
	}
}
