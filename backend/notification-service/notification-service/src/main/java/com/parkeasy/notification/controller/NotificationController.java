package com.parkeasy.notification.controller;

import com.parkeasy.notification.dto.NotificationResponse;
import com.parkeasy.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public List<NotificationResponse> my(Authentication auth) {
        return notificationService.myNotifications((Long) auth.getPrincipal());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(Authentication auth, @PathVariable Long id) {
        return notificationService.markRead((Long) auth.getPrincipal(), id);
    }
}