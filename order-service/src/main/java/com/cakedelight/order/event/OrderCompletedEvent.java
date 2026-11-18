
package com.cakedelight.order.event;

import java.math.BigDecimal;
import java.util.List;

import com.cakedelight.order.dto.response.OrderItemResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {

    private Long orderId;
    private String customerName;
    private String customerEmail;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
}
