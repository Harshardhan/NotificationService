package com.example.demo;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger =LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final ConsolidationServiceClient consolidationServiceClient;
    private final ProductServiceClient productServiceClient;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, JavaMailSender mailSender,
                                   ConsolidationServiceClient consolidationServiceClient, ProductServiceClient productServiceClient) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.consolidationServiceClient = consolidationServiceClient;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendNotification")
    @Retry(name = "notificationService")
    @RateLimiter(name = "notificationService")
    @Bulkhead(name = "notificationService", type = Bulkhead.Type.THREADPOOL)
    public Notification sendNotification(Notification notification) throws NotificationException, MessagingException {
        logger.info("Attempting to send notification to customer ID: {}", notification.getCustomerId());

        // Fetch order details from Consolidation Service
        Consolidation orderDetails = consolidationServiceClient.getConsolidationDetails(notification.getOrderId());
        Product productDetails = productServiceClient.getProductDetails(notification.getId());

        // Validate email before sending notification
        if (notification.getEmail() == null || notification.getEmail().isEmpty()) {
            logger.error("Email address is missing for customer ID: {}", notification.getCustomerId());
            throw new NotificationException("Email address is required for email notifications.");
        }

        // Save notification to the database
        Notification savedNotification = notificationRepository.save(
                Notification.builder()
                        .customerId(notification.getCustomerId())
                        .orderId(notification.getOrderId())
                        .orderReference(notification.getOrderReference())
                        .message(notification.getMessage())
                        .email(notification.getEmail())
                        .address(notification.getAddress())
                        .paymentMethod(notification.getPaymentMethod())
                        .price(notification.getPrice())
                        .productName(productDetails.getProductName())
                        .quantity(notification.getQuantity())
                        .type(notification.getType())
                        .sentAt(LocalDateTime.now())
                        .sent(false)
                        .build()
        );

        if (notification.getType() == NotificationType.EMAIL) {
            validateEmail(notification.getEmail());
            sendEmail(notification.getEmail(), "Order Notification", notification.getMessage());

            // Mark notification as sent
            savedNotification = savedNotification.toBuilder().sent(true).build();
            notificationRepository.save(savedNotification);
        }

        logger.info("Notification successfully sent to customer ID: {}", notification.getCustomerId());
        return savedNotification;
    }
    
    // Fallback methods and other service methods remain the same...

    public Notification fallbackSendNotification(Notification notification, Throwable t) {
        logger.error("⚠️ Fallback: Could not send notification for customer ID {} due to {}", notification.getCustomerId(), t.getMessage());

        return Notification.builder()
                .customerId(notification.getCustomerId())
                .orderId(notification.getOrderId())
                .orderReference(notification.getOrderReference())
                .message("Fallback: Notification could not be sent")
                .email(notification.getEmail())
                .type(notification.getType())
                .sentAt(LocalDateTime.now())
                .sent(false)
                .build();
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendOrderNotification")
    @Retry(name = "notificationService")
    @RateLimiter(name = "notificationService")
    @Bulkhead(name = "notificationService", type = Bulkhead.Type.THREADPOOL)
    public Notification sendOrderNotification(Long orderId, NotificationType type, String message) throws NotificationNotFoundException, MessagingException, NotificationException {
        logger.info("Sending order notification for Order ID: {}", orderId);

        Notification notification = notificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for Order ID: " + orderId));

        if (type == NotificationType.EMAIL) {
            validateEmail(notification.getEmail());
            sendEmail(notification.getEmail(), "Order Update", message);
        }

        Notification updatedNotification = notification.toBuilder()
                .message(message)
                .sentAt(LocalDateTime.now())
                .sent(type == NotificationType.EMAIL)
                .build();

        return notificationRepository.save(updatedNotification);
    }

    public Notification fallbackSendOrderNotification(Long orderId, NotificationType type, String message, Throwable t) {
        logger.error("⚠️ Fallback: Could not send order notification for Order ID {} due to {}", orderId, t.getMessage());

        return Notification.builder()
                .orderId(orderId)
                .message("Fallback: Unable to send order update")
                .type(type)
                .sent(false)
                .sentAt(LocalDateTime.now())
                .build();
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        logger.info("Sending email to: {}", to);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        mailSender.send(message);

        logger.info("Email sent successfully to {}", to);
    }

    private void validateEmail(String email) throws NotificationException {
        if (email == null || email.isEmpty()) {
            throw new NotificationException("Email address is required for email notifications");
        }
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getNotificationsByCustomer(Long customerId) {
        return notificationRepository.findByCustomerId(customerId);
    }

    @Override
    public Optional<Notification> getNotificationsByOrder(Long orderId) {
        return notificationRepository.findByOrderId(orderId);
    }

    @Override
    public void markNotificationAsSent(Long notificationId) throws NotificationNotFoundException {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for ID: " + notificationId));

        Notification updatedNotification = notification.toBuilder().sent(true).build();
        notificationRepository.save(updatedNotification);
    }
}
