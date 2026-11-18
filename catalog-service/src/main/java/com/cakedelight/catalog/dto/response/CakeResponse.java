
package com.cakedelight.catalog.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing cake information returned by the API.
 * It keeps the internal database entity separate from API responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CakeResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private Boolean available;
    private String imageReference;

    }

