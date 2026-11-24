package com.parkeasy.analytics.dto;

public record ParkingStat(
        Long parkingId,
        String name,
        long totalSpots,
        long freeSpots,
        double occupancyRate) {
}