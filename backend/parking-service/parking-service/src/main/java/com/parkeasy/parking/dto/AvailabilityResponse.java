package com.parkeasy.parking.dto;

public record AvailabilityResponse(
        Long parkingId,
        long totalSpots,
        long freeSpots,
        long occupiedSpots,
        long outOfServiceSpots,
        double occupancyRate) {
}