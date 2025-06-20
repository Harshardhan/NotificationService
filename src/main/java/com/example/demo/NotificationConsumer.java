package com.example.demo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class NotificationConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic}", groupId = "notification-service")
    public void listen(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            System.out.println("📩 Received Order with customerId: " + order.getCustomerId());

            // Trigger actual notification logic using order object
            // For example: sendEmail(order.getCustomerId(), order.getOrderStatus());

        } catch (Exception e) {
            System.err.println("❌ Failed to parse order JSON: " + e.getMessage());
        }
    }
    
    @KafkaListener(topics = "${kafka.topic}", groupId = "notification-service", containerFactory = "kafkaListenerContainerFactory")
    public void listen(Order order) {
        System.out.println("📩 Received Order with customerId: " + order.getCustomerId());

        // Trigger actual notification logic
        // e.g., sendEmail(order.getEmail(), order.getOrderReference());
    }

}
