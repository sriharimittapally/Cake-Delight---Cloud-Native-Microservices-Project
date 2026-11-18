package com.cakedelight.order.client.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogCakeResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Boolean available;
    private String imageReference;
}