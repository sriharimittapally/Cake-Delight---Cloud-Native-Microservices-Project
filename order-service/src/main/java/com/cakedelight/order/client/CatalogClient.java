
package com.cakedelight.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.cakedelight.order.client.dto.CatalogCakeResponse;

@Component
public class CatalogClient {

	private static final Logger logger = LoggerFactory.getLogger(CatalogClient.class);

	private final RestClient restClient;

	/**
	 * Initializes the RestClient with the Catalog Service base URL.
	 */
	public CatalogClient(RestClient.Builder builder, @Value("${catalog.service.url}") String catalogServiceUrl) {

		this.restClient = builder.baseUrl(catalogServiceUrl).build();
	}

	/**
	 * Retrieves cake details from the Catalog Service using the cake ID.
	 */
	public CatalogCakeResponse getCakeById(Long cakeId) {

		logger.debug("Fetching cake details from Catalog Service for cake ID: {}", cakeId);

		CatalogCakeResponse cake = restClient.get().uri("/api/cakes/{id}", cakeId).retrieve()
				.body(CatalogCakeResponse.class);

		logger.debug("Successfully retrieved cake details for cake ID: {}", cakeId);

		return cake;
	}
}
