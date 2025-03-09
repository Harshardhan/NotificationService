package com.example.demo;


import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody Notification notification) {
        logger.info("Received request to send notification to customer ID: {}", notification.getCustomerId());
        try {
            Notification sentNotification = notificationService.sendNotification(notification);
            logger.info("Notification sent successfully to customer ID: {}", notification.getCustomerId());
            return ResponseEntity.ok(sentNotification);
        } catch (NotificationException e) {
            logger.error("Failed to send notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/send-order/{orderId}")
    public ResponseEntity<?> sendOrderNotification(
            @PathVariable Long orderId,
            @RequestParam Notificationtype type,
            @RequestParam String message) throws MessagingException {
        logger.info("Received request to send order notification for Order ID: {}", orderId);
        try {
            Notification notification = notificationService.sendOrderNotification(orderId, type, message);
            logger.info("Order notification sent successfully for Order ID: {}", orderId);
            return ResponseEntity.ok(notification);
        } catch (NotificationNotFoundException e) {
            logger.error("Notification not found for Order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (NotificationException e) {
            logger.error("Failed to send order notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllNotifications() {
        logger.info("Received request to fetch all notifications");
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            logger.info("Fetched {} notifications", notifications.size());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            logger.error("Failed to fetch notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getNotificationsByCustomer(@PathVariable Long customerId) {
        logger.info("Received request to fetch notifications for Customer ID: {}", customerId);
        try {
            List<Notification> notifications = notificationService.getNotificationsByCustomer(customerId);
            logger.info("Fetched {} notifications for Customer ID: {}", notifications.size(), customerId);
            return ResponseEntity.ok(notifications);
        } catch (NotificationNotFoundException e) {
            logger.error("No notifications found for Customer ID: {}", customerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getNotificationsByOrder(@PathVariable Long orderId) {
        logger.info("Received request to fetch notifications for Order ID: {}", orderId);
        try {
            Optional<Notification> notification = notificationService.getNotificationsByOrder(orderId);
            logger.info("Fetched notification for Order ID: {}", orderId);
            return ResponseEntity.ok(notification);
        } catch (NotificationNotFoundException e) {
            logger.error("No notifications found for Order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/mark-sent/{notificationId}")
    public ResponseEntity<?> markNotificationAsSent(@PathVariable Long notificationId) {
        logger.info("Received request to mark notification ID {} as sent", notificationId);
        try {
            notificationService.markNotificationAsSent(notificationId);
            logger.info("Notification ID {} marked as sent", notificationId);
            return ResponseEntity.ok().build();
        } catch (NotificationNotFoundException e) {
            logger.error("Notification ID {} not found", notificationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to mark notification as sent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
