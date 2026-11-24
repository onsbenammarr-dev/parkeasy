package com.parkeasy.notification.controller;

import com.parkeasy.notification.dto.InternalNotificationRequest;
import com.parkeasy.notification.dto.NotificationResponse;
import com.parkeasy.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;
    private final String internalKey;

    public InternalNotificationController(NotificationService notificationService,
                                          @Value("${internal.api-key}") String internalKey) {
        this.notificationService = notificationService;
        this.internalKey = internalKey;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @RequestHeader(value = "X-Internal-Key", required = false) String key,
            @Valid @RequestBody InternalNotificationRequest request) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
        return notificationService.create(request.userId(), request.message());
    }
}