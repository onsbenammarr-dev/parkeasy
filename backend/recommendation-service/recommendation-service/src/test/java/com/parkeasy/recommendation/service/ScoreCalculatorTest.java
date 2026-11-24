package com.parkeasy.recommendation.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreCalculatorTest {

    @Test
    void lesPonderationsSontCellesDuCahierDesCharges() {
        assertEquals(35.0, ScoreCalculator.score(100, 0, 0, 0), 1e-9);
        assertEquals(30.0, ScoreCalculator.score(0, 100, 0, 0), 1e-9);
        assertEquals(25.0, ScoreCalculator.score(0, 0, 100, 0), 1e-9);
        assertEquals(10.0, ScoreCalculator.score(0, 0, 0, 100), 1e-9);
        assertEquals(100.0, ScoreCalculator.score(100, 100, 100, 100), 1e-9);
    }

    @Test
    void scoreDeDistance() {
        assertEquals(100.0, ScoreCalculator.distanceScore(0, 5), 1e-9);
        assertEquals(50.0, ScoreCalculator.distanceScore(2.5, 5), 1e-9);
        assertEquals(0.0, ScoreCalculator.distanceScore(8, 5), 1e-9);
    }

    @Test
    void scoreDePrix() {
        assertEquals(100.0, ScoreCalculator.priceScore(1.5, 1.5, 3.0), 1e-9);
        assertEquals(0.0, ScoreCalculator.priceScore(3.0, 1.5, 3.0), 1e-9);
        assertEquals(100.0, ScoreCalculator.priceScore(2.0, 2.0, 2.0), 1e-9);
    }

    @Test
    void scoreDeDisponibilite() {
        assertEquals(80.0, ScoreCalculator.availabilityScore(8, 10, 0), 1e-9);
        assertEquals(0.0, ScoreCalculator.availabilityScore(0, 3, 3), 1e-9);
    }
}