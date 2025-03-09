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

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public Notification sendNotification(Notification notification) throws NotificationException {
        logger.info("Attempting to send notification to customer ID: {}", notification.getCustomerId());

        try {
            // Save notification to DB
            notification.setSentAt(LocalDateTime.now());
            notification.setSent(false);
            Notification savedNotification = notificationRepository.save(notification);

            // Sending email
            if (notification.getType() == Notificationtype.EMAIL) {
                sendEmail(notification.getEmail(), "Order Notification", notification.getMessage());
                savedNotification.setSent(true);
            }

            logger.info("Notification successfully sent to customer ID: {}", notification.getCustomerId());
            return notificationRepository.save(savedNotification);
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage());
            throw new NotificationException("Error sending notification", e);
        }
    }

    @Override
    public Notification sendOrderNotification(Long orderId, Notificationtype type, String message)
            throws NotificationNotFoundException, NotificationException, MessagingException {
        logger.info("Sending order notification for Order ID: {}", orderId);

        Optional<Notification> optionalNotification = notificationRepository.findByOrderId(orderId);
        if (!optionalNotification.isPresent()) {
            logger.warn("No notification found for Order ID: {}", orderId);
            throw new NotificationNotFoundException("Notification not found for Order ID: " + orderId);
        }

        Notification notification = optionalNotification.get();
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setSent(false);

        if (type == Notificationtype.EMAIL) {
            sendEmail(notification.getEmail(), "Order Update", message);
            notification.setSent(true);
        }

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getAllNotifications() {
        logger.info("Fetching all notifications from DB");
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> getNotificationsByCustomer(Long customerId) throws NotificationNotFoundException {
        logger.info("Fetching notifications for Customer ID: {}", customerId);
        List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
        if (notifications.isEmpty()) {
            logger.warn("No notifications found for Customer ID: {}", customerId);
            throw new NotificationNotFoundException("No notifications found for Customer ID: " + customerId);
        }
        return notifications;
    }

    @Override
    public Optional<Notification> getNotificationsByOrder(Long orderId) throws NotificationNotFoundException {
        logger.info("Fetching notifications for Order ID: {}", orderId);
        Optional<Notification> notifications = notificationRepository.findByOrderId(orderId);
        if (notifications.isEmpty()) {
            logger.warn("No notifications found for Order ID: {}", orderId);
            throw new NotificationNotFoundException("No notifications found for Order ID: " + orderId);
        }
        return notifications;
    }

    @Override
    public void markNotificationAsSent(Long notificationId) throws NotificationNotFoundException {
        logger.info("Marking notification ID {} as sent", notificationId);
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (!optionalNotification.isPresent()) {
            logger.warn("Notification ID {} not found", notificationId);
            throw new NotificationNotFoundException("Notification ID not found: " + notificationId);
        }

        Notification notification = optionalNotification.get();
        notification.setSent(true);
        notificationRepository.save(notification);
        logger.info("Notification ID {} marked as sent", notificationId);
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
}
