package com.example.demo;

import java.util.List;

import java.util.Optional;
import jakarta.mail.MessagingException;

public interface NotificationService {

    // Send a notification (Email, SMS, Push)
    Notification sendNotification(Notification notification)throws NotificationException;

    // Send a notification based on order details
    Notification sendOrderNotification(Long orderId, Notificationtype type, String message)throws NotificationNotFoundException, NotificationException, MessagingException;

    // Get all notifications
    List<Notification> getAllNotifications();

    // Get notifications for a specific customer
    List<Notification> getNotificationsByCustomer(Long customerId)throws NotificationNotFoundException;

    // Get notifications for a specific order
    Optional<Notification> getNotificationsByOrder(Long orderId)throws NotificationNotFoundException;

    // Mark a notification as sent
    void markNotificationAsSent(Long notificationId)throws NotificationNotFoundException;
}
