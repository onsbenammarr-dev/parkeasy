package com.parkeasy.notification.service;

import com.parkeasy.notification.dto.NotificationResponse;
import com.parkeasy.notification.entity.Notification;
import com.parkeasy.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public NotificationResponse create(Long userId, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return toResponse(repository.save(n));
    }

    public List<NotificationResponse> myNotifications(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    public NotificationResponse markRead(Long userId, Long id) {
        Notification n = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
        n.setRead(true);
        return toResponse(repository.save(n));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}