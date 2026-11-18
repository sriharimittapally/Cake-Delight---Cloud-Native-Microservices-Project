
package com.cakedelight.order.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cakedelight.order.dto.response.OrderItemResponse;
import com.cakedelight.order.dto.response.OrderResponse;
import com.cakedelight.order.entity.Order;
import com.cakedelight.order.entity.OrderItem;

@Component
public class OrderMapper {

    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {

        OrderItemResponse response = new OrderItemResponse();

        response.setOrderItemId(orderItem.getId());
        response.setCakeId(orderItem.getCakeId());
        response.setCakeName(orderItem.getCakeName());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setQuantity(orderItem.getQuantity());
        response.setSubtotal(orderItem.getSubtotal());

        return response;
    }

    public OrderResponse toOrderResponse(Order order) {

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem orderItem : order.getItems()) {
            itemResponses.add(toOrderItemResponse(orderItem));
        }

        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(itemResponses);

        return response;
    }
}
