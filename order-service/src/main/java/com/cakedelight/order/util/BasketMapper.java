package com.cakedelight.order.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cakedelight.order.dto.response.BasketItemResponse;
import com.cakedelight.order.dto.response.BasketResponse;
import com.cakedelight.order.entity.Basket;
import com.cakedelight.order.entity.BasketItem;

@Component
public class BasketMapper {

    public BasketItemResponse toBasketItemResponse(BasketItem item) {

        BigDecimal subtotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        BasketItemResponse response = new BasketItemResponse();

        response.setItemId(item.getId());
        response.setCakeId(item.getCakeId());
        response.setCakeName(item.getCakeName());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(subtotal);

        return response;
    }

    public BasketResponse toBasketResponse(Basket basket) {

        List<BasketItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BasketItem item : basket.getItems()) {

            BasketItemResponse itemResponse = toBasketItemResponse(item);

            itemResponses.add(itemResponse);
            totalAmount = totalAmount.add(itemResponse.getSubtotal());
        }

        BasketResponse response = new BasketResponse();

        response.setBasketId(basket.getBasketId());
        response.setStatus(basket.getStatus());
        response.setItems(itemResponses);
        response.setTotalAmount(totalAmount);

        return response;
    }
}