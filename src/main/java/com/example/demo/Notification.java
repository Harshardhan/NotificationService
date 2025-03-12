package com.example.demo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
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
    private String message;
    private String email;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private LocalDateTime sentAt;
    private boolean sent;
}