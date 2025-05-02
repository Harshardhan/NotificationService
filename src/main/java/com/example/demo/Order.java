package com.example.demo;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Order {
	private Long id;
	private Long customerId;
	private Long productId;
	private String productName;
	private String description;
	private int quantity;
	private BigDecimal price;
	private String orderType;
	private String orderReference;
	private String paymentMethod;
	private String email;
	private String address;
	
	private OrderStatus orderStatus;

}
