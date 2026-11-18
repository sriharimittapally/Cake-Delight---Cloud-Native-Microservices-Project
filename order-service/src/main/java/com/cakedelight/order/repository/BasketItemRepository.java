package com.cakedelight.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cakedelight.order.entity.BasketItem;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {

    Optional<BasketItem> findByBasketBasketIdAndCakeId(Long basketId, Long cakeId);

}