package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Consolidation {

    private Long id;

    private Long customerId;
    private Long orderId;

    private String orderReference;

    private String orderStatus;
    private String optimisedItems;
    private String optimisedQuantity;
    private BigDecimal optimisedTotalAmount;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private boolean isPaid;
    private String orderType;
    private String deliveryAddress;
    private String paymentMethod;
    private String transactionId;
    private String currency;
    private String remarks;

}
