package com.cakedelight.order.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemResponse {

    private Long itemId;
    private Long cakeId;
    private String cakeName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}