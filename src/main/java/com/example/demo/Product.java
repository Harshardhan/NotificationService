package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private Long id;

    private String productName;

    private String description;

    private BigDecimal price;

    private String currencyCode;

    private Integer availableQuantity;

    private String category;

    private LocalDate expiryDate;

    private LocalDate manufacturingDate;

    private String email;
    private boolean isActive = true;

}
