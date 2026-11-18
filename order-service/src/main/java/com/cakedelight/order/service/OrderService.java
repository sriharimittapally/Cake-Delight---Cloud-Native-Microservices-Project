
package com.cakedelight.order.service;

import java.util.List;

import com.cakedelight.order.dto.request.CheckoutRequest;
import com.cakedelight.order.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse checkout(Long basketId, CheckoutRequest request);

    List<OrderResponse> getAllOrders();

    OrderResponse getOrderById(Long orderId);
    
    List<OrderResponse> getOrdersByCustomerEmail(String email);
    
    boolean verifyCakePurchase(Long orderId, String email, Long cakeId);
}
