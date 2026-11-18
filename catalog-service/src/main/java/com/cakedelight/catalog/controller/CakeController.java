
package com.cakedelight.catalog.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cakedelight.catalog.dto.request.CreateCakeRequest;
import com.cakedelight.catalog.dto.response.CakeResponse;
import com.cakedelight.catalog.service.CakeService;

import jakarta.validation.Valid;

/**
 * Handles HTTP requests related to cake catalog operations.
 */
@RestController
@RequestMapping("/api/cakes")
public class CakeController {

	private final CakeService cakeService;

	public CakeController(CakeService cakeService) {
		this.cakeService = cakeService;
	}

	/**
	 * Retrieves all cakes or filters them using query parameters.
	 */
	@GetMapping
	public ResponseEntity<List<CakeResponse>> getAllCakes(@RequestParam(required = false) String name,
			@RequestParam(required = false) String category, @RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice) {

		List<CakeResponse> cakes = cakeService.getAllCakes(name, category, minPrice, maxPrice);

		return ResponseEntity.ok(cakes);
	}
	
	
	@GetMapping("/admin")
	public ResponseEntity<List<CakeResponse>> getAllCakesForAdmin() {

	    List<CakeResponse> cakes = cakeService.getAllCakesForAdmin();

	    return ResponseEntity.ok(cakes);
	}

	/**
	 * Retrieves a specific cake using its ID.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<CakeResponse> getCakeById(@PathVariable Long id) {

		CakeResponse cake = cakeService.getCakeById(id);

		return ResponseEntity.ok(cake);
	}

	/**
	 * Creates a new cake.
	 */
	@PostMapping
	public ResponseEntity<CakeResponse> addCake(@Valid @RequestBody CreateCakeRequest request) {

		CakeResponse createdCake = cakeService.addCake(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(createdCake);
	}

	
	@PatchMapping("/{id}/availability")
	public ResponseEntity<Void> updateAvailability(@PathVariable Long id) {

	    cakeService.updateAvailability(id);

	    return ResponseEntity.noContent().build();
	}
	
	/**
	 * Deletes a cake using its ID.
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCake(@PathVariable Long id) {

		cakeService.deleteCake(id);

		return ResponseEntity.noContent().build();
	}
}
