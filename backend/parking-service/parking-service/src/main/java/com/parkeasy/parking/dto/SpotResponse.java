package com.parkeasy.parking.dto;

public record SpotResponse(
        Long id,
        Long parkingId,
        String spotNumber,
        String type,
        String physicalStatus) {
}