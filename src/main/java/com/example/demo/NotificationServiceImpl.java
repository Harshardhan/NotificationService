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

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    @Override
    public Notification sendNotification(Notification notification) throws NotificationException {
        logger.info("Attempting to send notification to customer ID: {}", notification.getCustomerId());

        try {
            Notification savedNotification = Notification.builder()
                    .customerId(notification.getCustomerId())
                    .message(notification.getMessage())
                    .email(notification.getEmail())
                    .type(notification.getType())
                    .sentAt(LocalDateTime.now())
                    .sent(false)
                    .build();

            savedNotification = notificationRepository.save(savedNotification);

            if (notification.getType() == NotificationType.EMAIL) {
                sendEmail(notification.getEmail(), "Order Notification", notification.getMessage());
                savedNotification = savedNotification.toBuilder().sent(true).build();
            }

            logger.info("Notification successfully sent to customer ID: {}", notification.getCustomerId());
            return notificationRepository.save(savedNotification);
        } catch (Exception e) {
            throw new NotificationException("Error sending notification", e); // ✅ Removed duplicate logging
        }
    }

    @Override
    public Notification sendOrderNotification(Long orderId, NotificationType type, String message)
            throws NotificationNotFoundException, NotificationException {
        logger.info("Sending order notification for Order ID: {}", orderId);

        Notification notification = notificationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found for Order ID: " + orderId));

        Notification updatedNotification = notification.toBuilder()
                .message(message)
                .sentAt(LocalDateTime.now())
                .sent(false)
                .build();

        if (type == NotificationType.EMAIL) {
            try {
                sendEmail(notification.getEmail(), "Order Update", message);
                updatedNotification = updatedNotification.toBuilder().sent(true).build();
            } catch (MessagingException e) {
                throw new NotificationException("Error sending email notification", e); // ✅ Removed duplicate logging
            }
        }

        return notificationRepository.save(updatedNotification);
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
            throw new NotificationNotFoundException("No notifications found for Customer ID: " + customerId);
        }
        return notifications;
    }

    @Override
    public Optional<Notification> getNotificationsByOrder(Long orderId) throws NotificationNotFoundException {
        logger.info("Fetching notifications for Order ID: {}", orderId);

        Optional<Notification> notificationOptional = notificationRepository.findByOrderId(orderId);

        if (notificationOptional.isEmpty()) {
            throw new NotificationNotFoundException("No notifications found for Order ID: " + orderId);
        }

        return notificationOptional;
    }

    @Override
    public void markNotificationAsSent(Long notificationId) throws NotificationNotFoundException {
        logger.info("Marking notification ID {} as sent", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification ID not found: " + notificationId));

        Notification updatedNotification = notification.toBuilder().sent(true).build();
        notificationRepository.save(updatedNotification);

        logger.info("Notification ID {} marked as sent", notificationId);
    }

    private void sendEmail(String to, String subject, String text) throws MessagingException {
        if (to == null || to.isEmpty()) {
            throw new MessagingException("Email address is null or empty"); // ✅ Removed unnecessary logging
        }

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
