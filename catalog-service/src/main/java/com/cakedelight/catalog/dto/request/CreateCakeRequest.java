
package com.cakedelight.catalog.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used when creating a new cake.
 * Contains only the information that the client is allowed to provide.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCakeRequest {

    @NotBlank(message = "Cake name is required")
    @Size(max = 150, message = "Cake name cannot exceed 150 characters")
    private String name;

    private String description;

    @NotBlank(message = "Cake category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotNull(message = "Cake price is required")
    @DecimalMin(value = "0.01", message = "Cake price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Cake availability is required")
    private Boolean available;

    @Size(max = 500, message = "Image reference cannot exceed 500 characters")
    private String imageReference;

    // Getters and setters

    
}
