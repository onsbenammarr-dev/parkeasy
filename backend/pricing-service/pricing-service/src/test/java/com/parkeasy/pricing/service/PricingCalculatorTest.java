package com.parkeasy.pricing.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingCalculatorTest {

    @Test
    void exempleDuCahierDesCharges() {
        BigDecimal price = PricingCalculator.hourlyPrice(new BigDecimal("2.0"), 0.8, 0.5, 1.0, 2.0);
        assertEquals(new BigDecimal("2.80"), price);
    }

    @Test
    void parkingVideGardeLeTarifDeBase() {
        BigDecimal price = PricingCalculator.hourlyPrice(new BigDecimal("2.0"), 0.0, 0.5, 1.0, 2.0);
        assertEquals(new BigDecimal("2.00"), price);
    }

    @Test
    void leTarifNeDepassePasLeMaximum() {
        BigDecimal price = PricingCalculator.hourlyPrice(new BigDecimal("2.0"), 1.0, 5.0, 1.0, 2.0);
        assertEquals(new BigDecimal("4.00"), price);
    }
}