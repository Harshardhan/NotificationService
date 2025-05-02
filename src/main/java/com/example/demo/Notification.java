package com.example.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private Long orderId;
    private String message;
	private String productName;
	private String description;
	private int quantity;
	private BigDecimal price;
	private String orderType;
	private String orderReference;
	private String paymentMethod;
	private String email;
	private String address;

    @Enumerated(EnumType.STRING)

    private NotificationType type;

    private LocalDateTime sentAt;
    private boolean sent;  // Removed the duplicate isSent field
}