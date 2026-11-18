
package com.cakedelight.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.cakedelight.order.dto.request.CheckoutRequest;
import com.cakedelight.order.dto.response.OrderResponse;
import com.cakedelight.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/checkout/{basketId}")
	public ResponseEntity<OrderResponse> checkout(@PathVariable Long basketId,
			@Valid @RequestBody CheckoutRequest request) {

		OrderResponse response = orderService.checkout(basketId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> getAllOrders() {

		List<OrderResponse> orders = orderService.getAllOrders();

		return ResponseEntity.ok(orders);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {

		OrderResponse response = orderService.getOrderById(orderId);

		return ResponseEntity.ok(response);
	}

	// Get all orders of customer by email
	@GetMapping("/customer")
	public ResponseEntity<List<OrderResponse>> getOrdersByCustomerEmail(
	        @RequestParam String email) {

	    List<OrderResponse> orders =
	            orderService.getOrdersByCustomerEmail(email);

	    return ResponseEntity.ok(orders);
	}
	
	
	@GetMapping("/{orderId}/verify-purchase")
	public ResponseEntity<Boolean> verifyCakePurchase(
	        @PathVariable Long orderId,
	        @RequestParam String email,
	        @RequestParam Long cakeId) {

	    boolean isPurchased =
	            orderService.verifyCakePurchase(orderId, email, cakeId);

	    return ResponseEntity.ok(isPurchased);
	}
}
