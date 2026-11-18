
package com.cakedelight.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cakedelight.order.dto.request.AddBasketItemRequest;
import com.cakedelight.order.dto.request.UpdateBasketItemRequest;
import com.cakedelight.order.dto.response.BasketResponse;
import com.cakedelight.order.service.BasketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/baskets")
public class BasketController {

    private final BasketService basketService;

    // Constructor injection
    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    // Create a new basket
    @PostMapping
    public ResponseEntity<BasketResponse> createBasket() {

        BasketResponse response = basketService.createBasket();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Add a cake to the basket
    @PostMapping("/{basketId}/items")
    public ResponseEntity<BasketResponse> addItem(
            @PathVariable Long basketId,
            @Valid @RequestBody AddBasketItemRequest request) {

        BasketResponse response =
                basketService.addItem(basketId, request);

        return ResponseEntity.ok(response);
    }

    // Get basket details
    @GetMapping("/{basketId}")
    public ResponseEntity<BasketResponse> getBasket(
            @PathVariable Long basketId) {

        BasketResponse response = basketService.getBasket(basketId);

        return ResponseEntity.ok(response);
    }

    // Update basket item quantity
    @PutMapping("/{basketId}/items/{itemId}")
    public ResponseEntity<BasketResponse> updateItemQuantity(
            @PathVariable Long basketId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateBasketItemRequest request) {

        BasketResponse response = basketService.updateItemQuantity(
                basketId, itemId, request);

        return ResponseEntity.ok(response);
    }

    // Remove a basket item
    @DeleteMapping("/{basketId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long basketId,
            @PathVariable Long itemId) {

        basketService.removeItem(basketId, itemId);

        return ResponseEntity.noContent().build();
    }
}
