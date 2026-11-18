
package com.cakedelight.order.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cakedelight.order.client.CatalogClient;
import com.cakedelight.order.client.dto.CatalogCakeResponse;
import com.cakedelight.order.dto.request.AddBasketItemRequest;
import com.cakedelight.order.dto.request.UpdateBasketItemRequest;
import com.cakedelight.order.dto.response.BasketResponse;
import com.cakedelight.order.entity.Basket;
import com.cakedelight.order.entity.BasketItem;
import com.cakedelight.order.repository.BasketItemRepository;
import com.cakedelight.order.repository.BasketRepository;
import com.cakedelight.order.service.BasketService;
import com.cakedelight.order.util.BasketMapper;

@Service
public class BasketServiceImpl implements BasketService {

	private static final Logger logger = LoggerFactory.getLogger(BasketServiceImpl.class);

	private final BasketRepository basketRepository;
	private final BasketItemRepository basketItemRepository;
	private final CatalogClient catalogClient;
	private final BasketMapper basketMapper;

	public BasketServiceImpl(BasketRepository basketRepository, BasketItemRepository basketItemRepository,
			CatalogClient catalogClient, BasketMapper basketMapper) {

		this.basketRepository = basketRepository;
		this.basketItemRepository = basketItemRepository;
		this.catalogClient = catalogClient;
		this.basketMapper = basketMapper;
	}

	/**
	 * Creates a new active basket for the customer.
	 */
	@Override
	public BasketResponse createBasket() {

		Basket basket = new Basket();
		basket.setStatus("ACTIVE");

		Basket savedBasket = basketRepository.save(basket);

		logger.info("Basket created successfully. Basket ID: {}", savedBasket.getBasketId());

		return basketMapper.toBasketResponse(savedBasket);
	}

	/**
	 * Adds a cake to the basket or increases its quantity if it already exists.
	 */
	@Override
	@Transactional
	public BasketResponse addItem(Long basketId, AddBasketItemRequest request) {

		logger.info("Adding cake {} to basket {}", request.getCakeId(), basketId);

		Basket basket = findBasketOrThrow(basketId);
		checkBasketIsActive(basket);

		// Retrieve cake details and verify availability through Catalog Service
		CatalogCakeResponse cake = catalogClient.getCakeById(request.getCakeId());

		if (cake.getAvailable() == null || !cake.getAvailable()) {
			logger.warn("Cake {} is unavailable", request.getCakeId());

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cake is not available");
		}

		Optional<BasketItem> existingItem = basketItemRepository.findByBasketBasketIdAndCakeId(basketId,
				request.getCakeId());

		if (existingItem.isPresent()) {

			BasketItem item = existingItem.get();

			item.setQuantity(item.getQuantity() + request.getQuantity());

			item.setCakeName(cake.getName());
			item.setUnitPrice(cake.getPrice());

			basketItemRepository.save(item);

			logger.info("Updated cake {} quantity in basket {}", cake.getId(), basketId);

		} else {

			BasketItem item = new BasketItem();

			item.setBasket(basket);
			item.setCakeId(cake.getId());
			item.setCakeName(cake.getName());
			item.setUnitPrice(cake.getPrice());
			item.setQuantity(request.getQuantity());

			basketItemRepository.save(item);

			logger.info("Added cake {} to basket {}", cake.getId(), basketId);
		}

		return basketMapper.toBasketResponse(basket);
	}

	/**
	 * Retrieves a basket and its items using the basket ID.
	 */
	@Override
	@Transactional(readOnly = true)
	public BasketResponse getBasket(Long basketId) {

		logger.debug("Fetching basket with ID: {}", basketId);

		Basket basket = findBasketOrThrow(basketId);

		return basketMapper.toBasketResponse(basket);
	}

	/**
	 * Updates the quantity of an existing item in the basket.
	 */
	@Override
	@Transactional
	public BasketResponse updateItemQuantity(Long basketId, Long itemId, UpdateBasketItemRequest request) {

		logger.info("Updating item {} in basket {}", itemId, basketId);

		Basket basket = findBasketOrThrow(basketId);
		checkBasketIsActive(basket);

		BasketItem item = basketItemRepository.findById(itemId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket item not found"));

		// Ensure the item belongs to the requested basket
		if (!item.getBasket().getBasketId().equals(basketId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket item not found");
		}

		item.setQuantity(request.getQuantity());

		basketItemRepository.save(item);

		logger.info("Updated item {} quantity to {} in basket {}", itemId, request.getQuantity(), basketId);

		return basketMapper.toBasketResponse(basket);
	}

	/**
	 * Removes an existing item from the customer's basket.
	 */
	@Override
	@Transactional
	public void removeItem(Long basketId, Long itemId) {

		logger.info("Removing item {} from basket {}", itemId, basketId);

		Basket basket = findBasketOrThrow(basketId);
		checkBasketIsActive(basket);

		BasketItem item = basketItemRepository.findById(itemId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket item not found"));

		// Ensure the item belongs to the requested basket
		if (!item.getBasket().getBasketId().equals(basketId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket item not found");
		}

		basket.getItems().remove(item);

		logger.info("Removed item {} from basket {}", itemId, basketId);
	}

	/**
	 * Finds a basket by its ID or throws a not-found exception.
	 */
	private Basket findBasketOrThrow(Long basketId) {

		return basketRepository.findById(basketId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket not found"));
	}

	/**
	 * Checks whether the basket is active before allowing modifications.
	 */
	private void checkBasketIsActive(Basket basket) {

		if (!"ACTIVE".equalsIgnoreCase(basket.getStatus())) {

			logger.warn("Basket {} is not active", basket.getBasketId());

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Basket is not active");
		}
	}
}
