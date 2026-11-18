package com.cakedelight.rating.dto.response;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long id;
    private Long orderId;
    private Long cakeId;
    private String customerName;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
}