package com.parkeasy.reservation.controller;

import com.parkeasy.reservation.dto.ReservationRequest;
import com.parkeasy.reservation.dto.ReservationResponse;
import com.parkeasy.reservation.event.ReservationEventPublisher;
import com.parkeasy.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationEventPublisher publisher;

    public ReservationController(ReservationService reservationService, ReservationEventPublisher publisher) {
        this.reservationService = reservationService;
        this.publisher = publisher;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(Authentication auth, @Valid @RequestBody ReservationRequest request) {
        return reservationService.create((Long) auth.getPrincipal(), request);
    }

    @GetMapping("/my")
    public List<ReservationResponse> my(Authentication auth) {
        return reservationService.myReservations((Long) auth.getPrincipal());
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        ReservationResponse r = reservationService.cancel(userId, id);
        publisher.publish("CANCELLED", "USER", r.id(), userId, r.parkingId(), r.spotId(),
                r.startTime(), r.endTime(), r.totalPrice());
        return r;
    }
}