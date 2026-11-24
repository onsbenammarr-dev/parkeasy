package com.parkeasy.reservation.service;

import com.parkeasy.reservation.client.ParkingClient;
import com.parkeasy.reservation.client.ParkingClient.ParkingInfo;
import com.parkeasy.reservation.client.ParkingClient.SpotInfo;
import com.parkeasy.reservation.client.PricingClient;
import com.parkeasy.reservation.dto.ReservationRequest;
import com.parkeasy.reservation.dto.ReservationResponse;
import com.parkeasy.reservation.entity.Reservation;
import com.parkeasy.reservation.entity.ReservationStatus;
import com.parkeasy.reservation.entity.SpotLock;
import com.parkeasy.reservation.event.ReservationEventPublisher;
import com.parkeasy.reservation.exception.SlotUnavailableException;
import com.parkeasy.reservation.exception.SlotUnavailableException.SlotOption;
import com.parkeasy.reservation.exception.SlotUnavailableException.SpotOption;
import com.parkeasy.reservation.repository.ReservationRepository;
import com.parkeasy.reservation.repository.SpotLockRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ReservationService {

    public static final String CONFLICT_MESSAGE =
            "Désolé, cette place est déjà réservée pour cet horaire. "
            + "Veuillez choisir un autre créneau ou une autre place.";

    private static final Set<ReservationStatus> BLOCKING = Set.of(
            ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

    private final ReservationRepository repository;
    private final ParkingClient parkingClient;
    private final PricingClient pricingClient;
    private final ReservationWriter writer;
    private final SpotLockRepository lockRepository;
    private final ReservationEventPublisher publisher;

    public ReservationService(ReservationRepository repository, ParkingClient parkingClient,
                              PricingClient pricingClient, ReservationWriter writer,
                              SpotLockRepository lockRepository, ReservationEventPublisher publisher) {
        this.repository = repository;
        this.parkingClient = parkingClient;
        this.pricingClient = pricingClient;
        this.writer = writer;
        this.lockRepository = lockRepository;
        this.publisher = publisher;
    }

    public ReservationResponse create(Long userId, ReservationRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin doit être après le début");
        }
        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le début doit être dans le futur");
        }

        ParkingInfo parking = parkingClient.getParking(request.parkingId());
        if (!Boolean.TRUE.equals(parking.active())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce parking n'est pas actif");
        }

        List<SpotInfo> spots = parkingClient.getSpots(request.parkingId());
        SpotInfo spot = spots.stream()
                .filter(s -> s.id().equals(request.spotId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Place introuvable dans ce parking"));
        if ("OUT_OF_SERVICE".equals(spot.physicalStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette place est hors service");
        }

        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        long hours = (minutes + 59) / 60;
        BigDecimal basePrice = parking.hourlyRate().multiply(BigDecimal.valueOf(hours));
        BigDecimal totalPrice = pricingClient
                .estimateTotal(request.parkingId(), request.startTime(), request.endTime())
                .orElse(basePrice);

        ensureLock(request.spotId());

        Reservation draft = new Reservation();
        draft.setUserId(userId);
        draft.setParkingId(request.parkingId());
        draft.setSpotId(request.spotId());
        draft.setStartTime(request.startTime());
        draft.setEndTime(request.endTime());
        draft.setTotalPrice(totalPrice);
        draft.setStatus(ReservationStatus.PENDING);

        Reservation saved = writer.book(draft);

        if (saved.getStatus() == ReservationStatus.REJECTED) {
            publisher.publish("REJECTED", null, saved);
            throw conflict(request, spots);
        }
        return toResponse(saved);
    }

    public List<ReservationResponse> myReservations(Long userId) {
        return repository.findByUserIdOrderByStartTimeDesc(userId).stream().map(this::toResponse).toList();
    }

    public ReservationResponse cancel(Long userId, Long reservationId) {
        Reservation r = repository.findById(reservationId)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
        if (r.getStatus() == ReservationStatus.CANCELLED || r.getStatus() == ReservationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette réservation ne peut plus être annulée");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        return toResponse(repository.save(r));
    }

    /** Crée la ligne de verrou de la place si elle n'existe pas encore (sans erreur en cas de course). */
    private void ensureLock(Long spotId) {
        if (!lockRepository.existsById(spotId)) {
            try {
                lockRepository.save(new SpotLock(spotId));
            } catch (DataIntegrityViolationException e) {
                // Une autre requête vient de la créer : c'est exactement ce qu'on voulait.
            }
        }
    }

    private SlotUnavailableException conflict(ReservationRequest request, List<SpotInfo> spots) {
        List<Long> busy = repository.findBusySpotIds(
                request.parkingId(), request.startTime(), request.endTime(), BLOCKING);
        List<SpotOption> options = spots.stream()
                .filter(s -> !"OUT_OF_SERVICE".equals(s.physicalStatus()))
                .filter(s -> !s.id().equals(request.spotId()))
                .filter(s -> !busy.contains(s.id()))
                .map(s -> new SpotOption(s.id(), s.spotNumber()))
                .toList();
        return new SlotUnavailableException(CONFLICT_MESSAGE, options, nextFreeSlot(request));
    }

    /** Premier créneau de même durée, à partir de l'heure demandée, où la même place est libre. */
    private SlotOption nextFreeSlot(ReservationRequest request) {
        long minutes = Duration.between(request.startTime(), request.endTime()).toMinutes();
        LocalDateTime candidate = request.startTime();
        List<Reservation> busy = repository.findBySpotIdAndStatusInAndEndTimeAfterOrderByStartTimeAsc(
                request.spotId(), BLOCKING, request.startTime());
        for (Reservation r : busy) {
            boolean overlaps = r.getStartTime().isBefore(candidate.plusMinutes(minutes))
                    && r.getEndTime().isAfter(candidate);
            if (overlaps) {
                candidate = r.getEndTime();
            }
        }
        if (candidate.equals(request.startTime())) {
            return null;
        }
        return new SlotOption(candidate, candidate.plusMinutes(minutes));
    }

    private ReservationResponse toResponse(Reservation r) {
        return new ReservationResponse(r.getId(), r.getParkingId(), r.getSpotId(),
                r.getStartTime(), r.getEndTime(), r.getTotalPrice(), r.getStatus().name());
    }
}