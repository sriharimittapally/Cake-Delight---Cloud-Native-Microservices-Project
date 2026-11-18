
package com.cakedelight.rating.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.cakedelight.rating.client.OrderServiceClient;
import com.cakedelight.rating.dto.request.CreateRatingRequest;
import com.cakedelight.rating.dto.response.RatingResponse;
import com.cakedelight.rating.dto.response.RatingSummaryResponse;
import com.cakedelight.rating.entity.Rating;
import com.cakedelight.rating.repository.RatingRepository;
import com.cakedelight.rating.service.RatingService;

@Service
public class RatingServiceImpl implements RatingService {

	private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);

	private final RatingRepository ratingRepository;
	private final OrderServiceClient orderServiceClient;

	/**
	 * Initializes the RatingService with its required dependencies.
	 */
	public RatingServiceImpl(RatingRepository ratingRepository, OrderServiceClient orderServiceClient) {

		this.ratingRepository = ratingRepository;
		this.orderServiceClient = orderServiceClient;
	}

	/**
	 * Submits a rating after verifying the customer's purchase.
	 */
	@Override
	@Transactional
	public RatingResponse submitRating(CreateRatingRequest request) {

		logger.info("Submitting rating for order ID: {} and cake ID: {}", request.getOrderId(), request.getCakeId());

		// Verify that the customer purchased the cake.
		boolean isPurchased = orderServiceClient.verifyCakePurchase(request.getOrderId(), request.getCustomerEmail(),
				request.getCakeId());

		if (!isPurchased) {
			logger.warn("Rating rejected because purchase could not be verified. Order ID: {}, Cake ID: {}",
					request.getOrderId(), request.getCakeId());

			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only rate cakes you have purchased");
		}

		// Prevent duplicate ratings for the same cake in an order.
		boolean alreadyRated = ratingRepository.existsByOrderIdAndCakeId(request.getOrderId(), request.getCakeId());

		if (alreadyRated) {
			logger.warn("Rating already exists for order ID: {} and cake ID: {}", request.getOrderId(),
					request.getCakeId());

			throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already rated this cake in this order");
		}

		Rating rating = new Rating();

		rating.setOrderId(request.getOrderId());
		rating.setCakeId(request.getCakeId());
		rating.setCustomerName(request.getCustomerName());
		rating.setCustomerEmail(request.getCustomerEmail());
		rating.setRating(request.getRating());
		rating.setReview(request.getReview());

		Rating savedRating = ratingRepository.save(rating);

		logger.info("Rating submitted successfully. Rating ID: {}, Cake ID: {}", savedRating.getId(),
				savedRating.getCakeId());

		return mapToRatingResponse(savedRating);
	}

	/**
	 * Retrieves all ratings for a specific cake, ordered by creation date.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RatingResponse> getRatingsByCakeId(Long cakeId) {

		logger.debug("Fetching ratings for cake ID: {}", cakeId);

		List<Rating> ratings = ratingRepository.findByCakeIdOrderByCreatedAtDesc(cakeId);

		List<RatingResponse> responses = new ArrayList<>();

		for (Rating rating : ratings) {
			responses.add(mapToRatingResponse(rating));
		}

		logger.debug("Retrieved {} ratings for cake ID: {}", responses.size(), cakeId);

		return responses;
	}

	/**
	 * Calculates the average rating and total number of ratings for a cake.
	 */
	@Override
	@Transactional(readOnly = true)
	public RatingSummaryResponse getRatingSummary(Long cakeId) {

		logger.debug("Calculating rating summary for cake ID: {}", cakeId);

		Double averageRating = ratingRepository.findAverageRatingByCakeId(cakeId);

		long totalRatings = ratingRepository.countByCakeId(cakeId);

		if (averageRating == null) {
			averageRating = 0.0;
		}

		averageRating = Math.round(averageRating * 10.0) / 10.0;

		return new RatingSummaryResponse(cakeId, averageRating, totalRatings);
	}

	/**
	 * Retrieves rating summaries for all cakes that have ratings.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RatingSummaryResponse> getAllRatingSummaries() {

		logger.debug("Fetching rating summaries for all cakes");

		List<Long> cakeIds = ratingRepository.findDistinctCakeIds();

		List<RatingSummaryResponse> summaries = new ArrayList<>();

		for (Long cakeId : cakeIds) {
			summaries.add(getRatingSummary(cakeId));
		}

		logger.debug("Retrieved rating summaries for {} cakes", summaries.size());

		return summaries;
	}

	/**
	 * Retrieves all ratings submitted by a specific customer email.
	 */
	@Override
	@Transactional(readOnly = true)
	public List<RatingResponse> getRatingsByCustomerEmail(String email) {

		logger.debug("Fetching ratings submitted by a customer");

		List<Rating> ratings = ratingRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(email);

		List<RatingResponse> responses = new ArrayList<>();

		for (Rating rating : ratings) {
			responses.add(mapToRatingResponse(rating));
		}

		logger.debug("Retrieved {} ratings for the customer", responses.size());

		return responses;
	}

	/**
	 * Converts a Rating entity into a RatingResponse DTO.
	 */
	private RatingResponse mapToRatingResponse(Rating rating) {

		return new RatingResponse(rating.getId(), rating.getOrderId(), rating.getCakeId(), rating.getCustomerName(),
				rating.getRating(), rating.getReview(), rating.getCreatedAt());
	}
}
