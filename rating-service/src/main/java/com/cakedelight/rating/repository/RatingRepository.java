package com.cakedelight.rating.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cakedelight.rating.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByOrderIdAndCakeId(Long orderId, Long cakeId);

    List<Rating> findByCakeIdOrderByCreatedAtDesc(Long cakeId);

    Optional<Rating> findByOrderIdAndCakeId(Long orderId, Long cakeId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.cakeId = :cakeId")
    Double findAverageRatingByCakeId(@Param("cakeId") Long cakeId);

    long countByCakeId(Long cakeId);
    
    @Query("SELECT DISTINCT r.cakeId FROM Rating r")
    List<Long> findDistinctCakeIds();
    
    
    List<Rating> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
            String customerEmail);
}