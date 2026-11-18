
package com.cakedelight.catalog.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cakedelight.catalog.dto.request.CreateCakeRequest;
import com.cakedelight.catalog.dto.response.CakeResponse;
import com.cakedelight.catalog.entity.Cake;
import com.cakedelight.catalog.mapper.CakeMapper;
import com.cakedelight.catalog.repository.CakeRepository;
import com.cakedelight.catalog.service.CakeService;

/**
 * Implements the business operations of the Cake Catalog Service.
 */
@Service
@Transactional
public class CakeServiceImpl implements CakeService {

	private static final Logger logger = LoggerFactory.getLogger(CakeServiceImpl.class);

	private final CakeRepository cakeRepository;
	private final CakeMapper cakeMapper;

	public CakeServiceImpl(CakeRepository cakeRepository, CakeMapper cakeMapper) {
		this.cakeRepository = cakeRepository;
		this.cakeMapper = cakeMapper;
	}

	/**
	 * Retrieves cakes based on optional filters.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CakeResponse> getAllCakes(String name, String category, BigDecimal minPrice, BigDecimal maxPrice) {

		logger.info("Fetching cakes using search filters");

		logger.debug("Search criteria - name: {}, category: {}, minPrice: {}, maxPrice: {}", name, category, minPrice,
				maxPrice);

		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {

			logger.warn("Invalid cake search price range. minPrice: {}, maxPrice: {}", minPrice, maxPrice);

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Minimum price cannot be greater than maximum price");
		}

		List<Cake> cakes = cakeRepository.searchCakes(name, category, minPrice, maxPrice);

		logger.info("Cake search completed successfully. Results found: {}", cakes.size());

		return cakes.stream().map(cake -> cakeMapper.toResponse(cake)).toList();
	}

	/**
	 * Retrieves a cake using its ID.
	 */
	@Override
	@Transactional(readOnly = true)
	public CakeResponse getCakeById(Long id) {

		logger.debug("Fetching cake details for ID: {}", id);

		Cake cake = findCakeOrThrow(id);

		logger.debug("Cake details retrieved successfully for ID: {}", id);

		return cakeMapper.toResponse(cake);
	}

	/**
	 * Creates and saves a new cake.
	 */
	@Override
	public CakeResponse addCake(CreateCakeRequest request) {

		logger.info("Creating a new cake: {}", request.getName());

		Cake cake = cakeMapper.toEntity(request);

		Cake savedCake = cakeRepository.save(cake);

		logger.info("Cake created successfully. ID: {}, Name: {}", savedCake.getId(), savedCake.getName());

		return cakeMapper.toResponse(savedCake);
	}

	/**
	 * Updates the availability of an existing cake.
	 */
	@Override
	public void updateAvailability(Long id) {

		logger.info("Updating availability for cake ID: {}", id);

		Cake cake = findCakeOrThrow(id);

		boolean currentAvailability = Boolean.TRUE.equals(cake.getAvailable());

		boolean updatedAvailability = !currentAvailability;

		cake.setAvailable(updatedAvailability);

		cakeRepository.save(cake);

		logger.info("Cake availability updated successfully. ID: {}, Previous availability: {}, New availability: {}",
				id, currentAvailability, updatedAvailability);
	}

	/**
	 * Retrieves all cakes for administrative purposes.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<CakeResponse> getAllCakesForAdmin() {

		logger.info("Fetching all cakes for admin");

		List<Cake> cakes = cakeRepository.findAll();

		logger.info("Admin cake retrieval completed. Total cakes found: {}", cakes.size());

		return cakes.stream().map(cake -> cakeMapper.toResponse(cake)).toList();
	}

	/**
	 * Deletes an existing cake using its ID.
	 */
	@Override
	public void deleteCake(Long id) {

		logger.info("Deleting cake with ID: {}", id);

		Cake cake = findCakeOrThrow(id);

		cakeRepository.delete(cake);

		logger.info("Cake deleted successfully. ID: {}, Name: {}", id, cake.getName());
	}

	/**
	 * Finds a cake by ID or throws an HTTP 404 exception.
	 */
	private Cake findCakeOrThrow(Long id) {

		logger.debug("Checking whether cake exists with ID: {}", id);

		return cakeRepository.findById(id).orElseThrow(() -> {

			logger.warn("Cake not found with ID: {}", id);

			return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cake not found with ID: " + id);
		});
	}
}
