package com.parkeasy.reservation.controller;

import com.parkeasy.reservation.entity.Reservation;
import com.parkeasy.reservation.entity.ReservationStatus;
import com.parkeasy.reservation.event.ReservationEventPublisher;
import com.parkeasy.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/internal/reservations")
public class InternalReservationController {

    public record InternalReservation(Long id, Long userId, BigDecimal totalPrice, String status,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Long parkingId, Long spotId) {
    }

    private final ReservationRepository repository;
    private final ReservationEventPublisher publisher;
    private final String internalKey;

    public InternalReservationController(ReservationRepository repository, ReservationEventPublisher publisher,
                                         @Value("${internal.api-key}") String internalKey) {
        this.repository = repository;
        this.publisher = publisher;
        this.internalKey = internalKey;
    }

    @GetMapping("/{id}")
    public InternalReservation get(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                   @PathVariable Long id) {
        checkKey(key);
        return toDto(find(id));
    }

    @PatchMapping("/{id}/confirm")
    public InternalReservation confirm(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                       @PathVariable Long id) {
        checkKey(key);
        Reservation r = find(id);
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une réservation en attente peut être confirmée");
        }
        r.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = repository.save(r);
        publisher.publish("CONFIRMED", null, saved);
        return toDto(saved);
    }

    @PatchMapping("/{id}/cancel")
    public InternalReservation cancel(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                      @PathVariable Long id) {
        checkKey(key);
        Reservation r = find(id);
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une réservation en attente peut être annulée ici");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = repository.save(r);
        publisher.publish("CANCELLED", "PAYMENT_FAILED", saved);
        return toDto(saved);
    }

    private void checkKey(String key) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
    }

    private Reservation find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
    }

    private InternalReservation toDto(Reservation r) {
        return new InternalReservation(r.getId(), r.getUserId(), r.getTotalPrice(), r.getStatus().name(),
                r.getStartTime(), r.getEndTime(), r.getParkingId(), r.getSpotId());
    }
}