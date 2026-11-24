package com.parkeasy.notification.controller;

import com.parkeasy.notification.dto.NotificationResult;
import com.parkeasy.notification.dto.ReservationEventRequest;
import com.parkeasy.notification.dto.WelcomeRequest;
import com.parkeasy.notification.service.EmailTemplates;
import com.parkeasy.notification.service.MailResult;
import com.parkeasy.notification.service.MailService;
import com.parkeasy.notification.service.NotificationService;
import com.parkeasy.notification.service.ReservationEventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/internal")
public class InternalEmailController {

    private final NotificationService notificationService;
    private final MailService mailService;
    private final ReservationEventService eventService;
    private final String internalKey;
    private final String appUrl;

    public InternalEmailController(NotificationService notificationService, MailService mailService,
                                   ReservationEventService eventService,
                                   @Value("${internal.api-key}") String internalKey,
                                   @Value("${parkeasy.frontend-url}") String appUrl) {
        this.notificationService = notificationService;
        this.mailService = mailService;
        this.eventService = eventService;
        this.internalKey = internalKey;
        this.appUrl = appUrl;
    }

    @PostMapping("/emails/welcome")
    public NotificationResult welcome(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                      @Valid @RequestBody WelcomeRequest request) {
        checkKey(key);
        String message = "Bienvenue sur ParkEasy, " + request.fullName() + " ! Ton compte est créé.";
        notificationService.create(request.userId(), message.length() <= 250 ? message : message.substring(0, 247) + "...");
        MailResult result = mailService.sendHtml(request.email(), "Bienvenue sur ParkEasy",
                EmailTemplates.welcome(request.fullName(), appUrl));
        return new NotificationResult(result.sent(), result.error());
    }

    @PostMapping("/reservation-events")
    public NotificationResult reservationEvent(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                               @Valid @RequestBody ReservationEventRequest request) {
        checkKey(key);
        try {
            return eventService.handle(request);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Événement invalide : " + e.getMessage());
        }
    }

    private void checkKey(String key) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
    }
}