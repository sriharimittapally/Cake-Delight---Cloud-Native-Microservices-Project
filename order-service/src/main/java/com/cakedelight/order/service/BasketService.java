package com.cakedelight.order.service;

import com.cakedelight.order.dto.request.AddBasketItemRequest;
import com.cakedelight.order.dto.request.UpdateBasketItemRequest;
import com.cakedelight.order.dto.response.BasketResponse;

public interface BasketService {

    BasketResponse createBasket();

    BasketResponse addItem(Long basketId, AddBasketItemRequest request);

    BasketResponse getBasket(Long basketId);

    BasketResponse updateItemQuantity(
            Long basketId,
            Long itemId,
            UpdateBasketItemRequest request
    );

    void removeItem(Long basketId, Long itemId);
}