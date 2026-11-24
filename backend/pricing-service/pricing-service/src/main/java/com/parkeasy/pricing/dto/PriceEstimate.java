package com.parkeasy.pricing.dto;

import java.math.BigDecimal;

public record PriceEstimate(
        Long parkingId,
        BigDecimal basePrice,
        double occupancyRate,
        BigDecimal hourlyPrice,
        long hours,
        BigDecimal totalPrice) {
}