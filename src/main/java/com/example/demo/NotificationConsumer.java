package com.example.demo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(topics = KafkaConfig.TOPIC, groupId = "notification-service")
    public void listen(String message) {
        System.out.println("📩 Received notification: " + message);
        // Trigger notification logic here
    }
}
