
package com.cakedelight.rating.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.cakedelight.rating.dto.request.CreateRatingRequest;
import com.cakedelight.rating.dto.response.RatingResponse;
import com.cakedelight.rating.dto.response.RatingSummaryResponse;
import com.cakedelight.rating.service.RatingService;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Submit a rating for a purchased cake
    @PostMapping
    public ResponseEntity<RatingResponse> submitRating(
            @Valid @RequestBody CreateRatingRequest request) {

        RatingResponse response = ratingService.submitRating(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all reviews for a particular cake
    @GetMapping("/cake/{cakeId}")
    public ResponseEntity<List<RatingResponse>> getRatingsByCakeId(
            @PathVariable Long cakeId) {

        List<RatingResponse> ratings =
                ratingService.getRatingsByCakeId(cakeId);

        return ResponseEntity.ok(ratings);
    }

    // Get average rating and total reviews for a particular cake
    @GetMapping("/cake/{cakeId}/summary")
    public ResponseEntity<RatingSummaryResponse> getRatingSummary(
            @PathVariable Long cakeId) {

        RatingSummaryResponse summary =
                ratingService.getRatingSummary(cakeId);

        return ResponseEntity.ok(summary);
    }

    // Get rating summaries for all rated cakes
    @GetMapping("/summary")
    public ResponseEntity<List<RatingSummaryResponse>> getAllRatingSummaries() {

        List<RatingSummaryResponse> summaries =
                ratingService.getAllRatingSummaries();

        return ResponseEntity.ok(summaries);
    }
    
    
    
 // Get all ratings submitted by a customer using email
    @GetMapping("/customer")
    public ResponseEntity<List<RatingResponse>> getRatingsByCustomerEmail(
            @RequestParam String email) {

        List<RatingResponse> ratings =
                ratingService.getRatingsByCustomerEmail(email);

        return ResponseEntity.ok(ratings);
    }
}
