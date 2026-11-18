package com.cakedelight.order.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketResponse {

    private Long basketId;
    private String status;
    private List<BasketItemResponse> items;
    private BigDecimal totalAmount;
}