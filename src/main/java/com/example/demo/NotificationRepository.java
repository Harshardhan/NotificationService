package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find all notifications for a specific customer
    List<Notification> findByCustomerId(Long customerId);

    // Find all notifications for a specific order
    Optional<Notification> findByOrderId(Long orderId);

    // Find all notifications by order reference
    List<Notification> findByOrderReference(String orderReference);

    // Find all notifications that are not yet sent
    List<Notification> findByIsSentFalse();

    // Find all notifications of a specific type (EMAIL, SMS, PUSH_NOTIFICATION)
    List<Notification> findByType(NotificationType type);

    // Find latest notification for a specific order
    Notification findTopByOrderIdOrderBySentAtDesc(Long orderId);
}
