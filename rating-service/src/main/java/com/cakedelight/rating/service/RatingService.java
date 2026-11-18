package com.cakedelight.rating.service;

import java.util.List;

import com.cakedelight.rating.dto.request.CreateRatingRequest;
import com.cakedelight.rating.dto.response.RatingResponse;
import com.cakedelight.rating.dto.response.RatingSummaryResponse;

public interface RatingService {

    RatingResponse submitRating(CreateRatingRequest request);

    List<RatingResponse> getRatingsByCakeId(Long cakeId);

    RatingSummaryResponse getRatingSummary(Long cakeId);

    List<RatingSummaryResponse> getAllRatingSummaries();
    
    List<RatingResponse> getRatingsByCustomerEmail(String email);
}