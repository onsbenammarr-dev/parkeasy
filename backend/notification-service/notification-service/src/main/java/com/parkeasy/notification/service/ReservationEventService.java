package com.parkeasy.notification.service;

import com.parkeasy.notification.client.IdentityClient;
import com.parkeasy.notification.client.IdentityClient.UserInfo;
import com.parkeasy.notification.client.ParkingClient;
import com.parkeasy.notification.dto.NotificationResult;
import com.parkeasy.notification.dto.ReservationEventRequest;
import com.parkeasy.notification.service.EmailTemplates.ReservationMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ReservationEventService {

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationService notificationService;
    private final MailService mailService;
    private final IdentityClient identityClient;
    private final ParkingClient parkingClient;
    private final String appUrl;

    public ReservationEventService(NotificationService notificationService, MailService mailService,
                                   IdentityClient identityClient, ParkingClient parkingClient,
                                   @Value("${parkeasy.frontend-url}") String appUrl) {
        this.notificationService = notificationService;
        this.mailService = mailService;
        this.identityClient = identityClient;
        this.parkingClient = parkingClient;
        this.appUrl = appUrl;
    }

    public NotificationResult handle(ReservationEventRequest e) {
        LocalDateTime start = LocalDateTime.parse(e.startTime());
        LocalDateTime end = LocalDateTime.parse(e.endTime());
        String parking = parkingClient.findParking(e.parkingId())
                .map(ParkingClient.ParkingInfo::name)
                .orElse("Parking n°" + e.parkingId());
        String spot = parkingClient.findSpotNumber(e.parkingId(), e.spotId()).orElse("n°" + e.spotId());
        String when = start.format(SHORT) + " → " + end.format(SHORT);
        boolean paymentFailed = "PAYMENT_FAILED".equals(e.reason());

        String inApp;
        if ("CONFIRMED".equals(e.type())) {
            inApp = "Réservation confirmée : " + parking + ", place " + spot + ", " + when + ".";
        } else if ("CANCELLED".equals(e.type())) {
            inApp = (paymentFailed ? "Paiement échoué, réservation annulée : " : "Réservation annulée : ")
                    + parking + ", place " + spot + ", " + when + ".";
        } else if ("REJECTED".equals(e.type())) {
            inApp = "Réservation refusée (place déjà réservée sur ce créneau) : "
                    + parking + ", place " + spot + ", " + when + ".";
        } else {
            throw new IllegalArgumentException("Type d'événement inconnu : " + e.type());
        }
        notificationService.create(e.userId(), shorten(inApp));

        Optional<UserInfo> user = identityClient.findUser(e.userId());
        if (user.isEmpty()) {
            return new NotificationResult(false, "Utilisateur introuvable : email non envoyé.");
        }
        ReservationMail mail = new ReservationMail(user.get().fullName(), parking, spot, start, end,
                e.totalPrice(), e.reservationId(), appUrl);

        String subject;
        String html;
        if ("CONFIRMED".equals(e.type())) {
            subject = "Réservation confirmée - ParkEasy";
            html = EmailTemplates.confirmed(mail);
        } else if ("CANCELLED".equals(e.type())) {
            subject = "Réservation annulée - ParkEasy";
            html = EmailTemplates.cancelled(mail, paymentFailed);
        } else {
            subject = "Réservation refusée - ParkEasy";
            html = EmailTemplates.rejected(mail);
        }
        MailResult result = mailService.sendHtml(user.get().email(), subject, html);
        return new NotificationResult(result.sent(), result.error());
    }

    private String shorten(String text) {
        return text.length() <= 250 ? text : text.substring(0, 247) + "...";
    }
}