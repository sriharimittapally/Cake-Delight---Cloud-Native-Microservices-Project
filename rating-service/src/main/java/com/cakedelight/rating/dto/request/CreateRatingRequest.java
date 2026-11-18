package com.cakedelight.rating.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Cake ID is required")
    private Long cakeId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 150, message = "Customer name cannot exceed 150 characters")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 1000, message = "Review cannot exceed 1000 characters")
    private String review;
}