package com.parkeasy.reservation.service;

import com.parkeasy.reservation.entity.Reservation;
import com.parkeasy.reservation.entity.ReservationStatus;
import com.parkeasy.reservation.repository.ReservationRepository;
import com.parkeasy.reservation.repository.SpotLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ReservationWriter {

    private static final Set<ReservationStatus> BLOCKING = Set.of(
            ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

    private final ReservationRepository repository;
    private final SpotLockRepository lockRepository;

    public ReservationWriter(ReservationRepository repository, SpotLockRepository lockRepository) {
        this.repository = repository;
        this.lockRepository = lockRepository;
    }

    /**
     * Une seule transaction à la fois peut réserver une même place : la ligne de verrou est bloquée
     * jusqu'à la fin de la transaction, et la vérification du chevauchement lit les données à jour.
     */
    @Transactional
    public Reservation book(Reservation draft) {
        lockRepository.findForUpdate(draft.getSpotId())
                .orElseThrow(() -> new IllegalStateException("Verrou de place introuvable"));
        boolean taken = !repository.findOverlappingForUpdate(
                draft.getSpotId(), draft.getStartTime(), draft.getEndTime(), BLOCKING).isEmpty();
        draft.setStatus(taken ? ReservationStatus.REJECTED : ReservationStatus.PENDING);
        return repository.save(draft);
    }
}