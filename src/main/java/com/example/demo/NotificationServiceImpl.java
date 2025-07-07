package com.example.demo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

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

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final ConsolidationServiceClient consolidationServiceClient;
    private final ProductServiceClient productServiceClient;

    @Autowired
    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            JavaMailSender mailSender,
            ConsolidationServiceClient consolidationServiceClient,
            ProductServiceClient productServiceClient) {
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
        logger.info("🚀 Starting notification process for customer ID: {}", notification.getCustomerId());
        if (notification.getProductId() == null) {
            logger.warn("⚠️ Product ID is null in notification, unable to fetch product details.");
        }


        // Validate email
        validateEmail(notification.getEmail());

        // Fetch product details (optional fallback)
        String productName = null;
        try {
            logger.debug("🔍 Fetching product with ID: {}", notification.getProductId());
            Product product = productServiceClient.getProductDetails(notification.getProductId());
            productName = product != null ? product.getProductName() : "Unknown Product";
        } catch (Exception e) {
            logger.warn("⚠️ Failed to fetch product details: {}", e.getMessage());

        }

        // Fetch order details (optional)
        try {
            Consolidation order = consolidationServiceClient.getConsolidationDetails(notification.getOrderId());
            logger.info("✅ Order fetched for order ID: {}", order.getOrderReference());
        } catch (Exception e) {
            logger.warn("⚠️ Could not fetch order details for ID {}: {}", notification.getOrderId(), e.getMessage());
        }

        // Save notification to DB
        Notification savedNotification = notificationRepository.save(
                Notification.builder()
                        .customerId(notification.getCustomerId())
                        .orderId(notification.getOrderId())
                        .productId(notification.getProductId())
                        .orderReference(notification.getOrderReference())
                        .message(notification.getMessage())
                        .email(notification.getEmail())
                        .address(notification.getAddress())
                        .paymentMethod(notification.getPaymentMethod())
                        .price(notification.getPrice())
                        .quantity(notification.getQuantity())
                        .productName(productName)
                        .type(notification.getType())
                        .sent(false)
                        .sentAt(LocalDateTime.now())
                        .build()
        );

        // Send Email
        if (notification.getType() == NotificationType.EMAIL) {
            logger.info("📧 Preparing to send email to {}", notification.getEmail());

            try {
            	String emailBody = String.format(
            		    "Dear Customer,\n\n" +
            		    "Your order has been placed successfully! 🧾\n\n" +
            		    "Order Details:\n" +
            		    "Order ID: %s\n" +
            		    "Order Reference: %s\n" +
            		    "Product: %s\n" +
            		    "Quantity: %d\n" +
            		    "Price: ₹%.2f\n" +
            		    "Payment Method: %s\n" +
            		    "Shipping Address: %s\n\n" +
            		    "Thank you for shopping with us!\n",
            		    notification.getOrderId(),
            		    notification.getOrderReference(),
            		    productName != null ? productName : "N/A",  // <-- use this instead
            		    notification.getQuantity(),
            		    notification.getPrice(),
            		    notification.getPaymentMethod(),
            		    notification.getAddress()
            		);
            		sendEmail(notification.getEmail(), "Order Confirmation", emailBody);
                logger.info("✅ Email sent successfully to {}", notification.getEmail());

                // Mark as sent
                savedNotification = savedNotification.toBuilder().sent(true).build();
                notificationRepository.save(savedNotification);
            } catch (MessagingException ex) {
                logger.error("❌ Failed to send email: {}", ex.getMessage(), ex);
                throw ex;
            }
        }

        return savedNotification;
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        mailSender.send(message);
    }

    private void validateEmail(String email) throws NotificationException {
        if (email == null || email.trim().isEmpty()) {
            throw new NotificationException("❌ Email address is required for notification.");
        }
    }

    public Notification fallbackSendNotification(Notification notification, Throwable t) {
        logger.error("🔁 Fallback: Failed to send notification for customer ID {}. Reason: {}", notification.getCustomerId(), t.getMessage());

        return Notification.builder()
                .customerId(notification.getCustomerId())
                .orderId(notification.getOrderId())
                .orderReference(notification.getOrderReference())
                .message("Fallback: Notification not sent")
                .email(notification.getEmail())
                .type(notification.getType())
                .sent(false)
                .sentAt(LocalDateTime.now())
                .build();
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

        Notification updated = notification.toBuilder().sent(true).build();
        notificationRepository.save(updated);
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "fallbackSendOrderNotification")
    @Retry(name = "notificationService")
    @RateLimiter(name = "notificationService")
    @Bulkhead(name = "notificationService", type = Bulkhead.Type.THREADPOOL)
    public Notification sendOrderNotification(Long orderId, NotificationType type, String message)
            throws NotificationNotFoundException, MessagingException, NotificationException {

        logger.info("📦 Sending update for order ID: {}", orderId);

        Notification notification = notificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for Order ID: " + orderId));

        if (type == NotificationType.EMAIL) {
            validateEmail(notification.getEmail());
            sendEmail(notification.getEmail(), "Order Update", message);
        }

        Notification updatedNotification = notification.toBuilder()
                .message(message)
                .sent(type == NotificationType.EMAIL)
                .sentAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(updatedNotification);
    }

    public Notification fallbackSendOrderNotification(Long orderId, NotificationType type, String message, Throwable t) {
        logger.error("🔁 Fallback: Could not send order update for Order ID {} due to {}", orderId, t.getMessage());

        return Notification.builder()
                .orderId(orderId)
                .message("Fallback: Unable to send order update")
                .type(type)
                .sent(false)
                .sentAt(LocalDateTime.now())
                .build();
    }
}
