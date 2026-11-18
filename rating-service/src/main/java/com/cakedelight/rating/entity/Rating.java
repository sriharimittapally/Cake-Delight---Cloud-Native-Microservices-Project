package com.cakedelight.rating.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_order_cake_rating",
            columnNames = {"order_id", "cake_id"}
        )
    }
)
@Data
@NoArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "cake_id", nullable = false)
    private Long cakeId;

    @Column(name = "customer_name", length = 150, nullable = false)
    private String customerName;

    @Column(name = "customer_email", length = 255, nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String review;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}