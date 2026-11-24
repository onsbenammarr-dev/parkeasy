package com.parkeasy.reservation.event;

import com.parkeasy.reservation.entity.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
public class ReservationEventPublisher {

    public record EventBody(String type, String reason, Long reservationId, Long userId, Long parkingId,
                            Long spotId, String startTime, String endTime, BigDecimal totalPrice) {
    }

    private static final Logger log = LoggerFactory.getLogger(ReservationEventPublisher.class);

    private final RestClient client;
    private final String internalKey;

    public ReservationEventPublisher(@Value("${notification.service.url}") String baseUrl,
                                     @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalKey = internalKey;
    }

    public void publish(String type, String reason, Reservation r) {
        publish(type, reason, r.getId(), r.getUserId(), r.getParkingId(), r.getSpotId(),
                r.getStartTime(), r.getEndTime(), r.getTotalPrice());
    }

    /** Envoi en arrière-plan : une panne de notification ne bloque ni n'annule jamais une réservation. */
    public void publish(String type, String reason, Long reservationId, Long userId, Long parkingId, Long spotId,
                        LocalDateTime start, LocalDateTime end, BigDecimal price) {
        EventBody body = new EventBody(type, reason, reservationId, userId, parkingId, spotId,
                start.toString(), end.toString(), price);
        CompletableFuture.runAsync(() -> {
            try {
                client.post()
                        .uri("/api/internal/reservation-events")
                        .header("X-Internal-Key", internalKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
            } catch (RestClientException e) {
                log.warn("Notification {} non envoyée pour la réservation {} : {}", type, reservationId, e.getMessage());
            }
        });
    }
}