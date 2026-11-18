package com.cakedelight.rating.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummaryResponse {

    private Long cakeId;
    private Double averageRating;
    private Long totalRatings;
}