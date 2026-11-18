
package com.cakedelight.catalog.service;

import java.math.BigDecimal;
import java.util.List;

import com.cakedelight.catalog.dto.request.CreateCakeRequest;
import com.cakedelight.catalog.dto.response.CakeResponse;

/**
 * Defines the business operations supported by the Cake Catalog Service.
 */
public interface CakeService {

    /**
     * Retrieves cakes using optional search filters.
     */
    List<CakeResponse> getAllCakes(
            String name,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    
    List<CakeResponse> getAllCakesForAdmin();
    /**
     * Retrieves a cake using its ID.
     */
    CakeResponse getCakeById(Long id);

    /**
     * Creates a new cake.
     */
    CakeResponse addCake(CreateCakeRequest request);
    
    
    /**
     * Updates the availability of an existing cake.
     */
    void updateAvailability(Long id);
    /**
     * Deletes a cake using its ID.
     */
    void deleteCake(Long id);
}
