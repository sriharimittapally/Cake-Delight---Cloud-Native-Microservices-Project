package com.cakedelight.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cakedelight.order.entity.Basket;

public interface BasketRepository extends JpaRepository<Basket, Long> {

}