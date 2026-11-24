package com.parkeasy.recommendation.dto;

import java.math.BigDecimal;

public record RecommendationResponse(
        Long parkingId,
        String name,
        String address,
        double distanceKm,
        BigDecimal hourlyRate,
        long freeSpots,
        double score,
        String justification) {
}