package com.parkeasy.reservation.exception;

import java.time.LocalDateTime;
import java.util.List;

public class SlotUnavailableException extends RuntimeException {

    public record SpotOption(Long id, String spotNumber) {
    }

    public record SlotOption(LocalDateTime start, LocalDateTime end) {
    }

    private final List<SpotOption> alternativeSpots;
    private final SlotOption nextSlot;

    public SlotUnavailableException(String message, List<SpotOption> alternativeSpots, SlotOption nextSlot) {
        super(message);
        this.alternativeSpots = alternativeSpots;
        this.nextSlot = nextSlot;
    }

    public List<SpotOption> getAlternativeSpots() {
        return alternativeSpots;
    }

    public SlotOption getNextSlot() {
        return nextSlot;
    }
}