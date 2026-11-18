
package com.cakedelight.catalog.mapper;

import org.springframework.stereotype.Component;

import com.cakedelight.catalog.dto.request.CreateCakeRequest;
import com.cakedelight.catalog.dto.response.CakeResponse;
import com.cakedelight.catalog.entity.Cake;

/**
 * Converts cake request DTOs into entities
 * and cake entities into response DTOs.
 */
@Component
public class CakeMapper {

    /**
     * Converts incoming cake creation data into a Cake entity.
     */
    public Cake toEntity(CreateCakeRequest request) {

        Cake cake = new Cake();

        cake.setName(request.getName());
        cake.setDescription(request.getDescription());
        cake.setCategory(request.getCategory());
        cake.setPrice(request.getPrice());
        cake.setAvailable(request.getAvailable());
        cake.setImageReference(request.getImageReference());

        return cake;
    }

    /**
     * Converts a Cake entity into a response DTO.
     */
    public CakeResponse toResponse(Cake cake) {

        CakeResponse response = new CakeResponse();

        response.setId(cake.getId());
        response.setName(cake.getName());
        response.setDescription(cake.getDescription());
        response.setCategory(cake.getCategory());
        response.setPrice(cake.getPrice());
        response.setAvailable(cake.getAvailable());
        response.setImageReference(cake.getImageReference());

        return response;
    }
}
