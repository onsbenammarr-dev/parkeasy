package com.parkeasy.pricing.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PricingCalculator {

    private PricingCalculator() {
    }

    /** P = P0 x (1 + alpha x O), borné entre minMultiplier x P0 et maxMultiplier x P0. */
    public static BigDecimal hourlyPrice(BigDecimal basePrice, double occupancy,
                                         double alpha, double minMultiplier, double maxMultiplier) {
        double o = Math.max(0.0, Math.min(1.0, occupancy));
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(alpha).multiply(BigDecimal.valueOf(o)));
        BigDecimal price = basePrice.multiply(factor);
        BigDecimal min = basePrice.multiply(BigDecimal.valueOf(minMultiplier));
        BigDecimal max = basePrice.multiply(BigDecimal.valueOf(maxMultiplier));
        if (price.compareTo(min) < 0) {
            price = min;
        }
        if (price.compareTo(max) > 0) {
            price = max;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}