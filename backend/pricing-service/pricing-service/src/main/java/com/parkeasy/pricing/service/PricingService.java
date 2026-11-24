package com.parkeasy.pricing.service;

import com.parkeasy.pricing.client.ParkingClient;
import com.parkeasy.pricing.client.ParkingClient.Availability;
import com.parkeasy.pricing.client.ParkingClient.ParkingInfo;
import com.parkeasy.pricing.dto.PriceEstimate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {

    private final ParkingClient parkingClient;
    private final double alpha;
    private final double minMultiplier;
    private final double maxMultiplier;

    public PricingService(ParkingClient parkingClient,
                          @Value("${pricing.alpha}") double alpha,
                          @Value("${pricing.min-multiplier}") double minMultiplier,
                          @Value("${pricing.max-multiplier}") double maxMultiplier) {
        this.parkingClient = parkingClient;
        this.alpha = alpha;
        this.minMultiplier = minMultiplier;
        this.maxMultiplier = maxMultiplier;
    }

    public PriceEstimate estimate(Long parkingId, LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fin doit être après le début");
        }
        ParkingInfo parking = parkingClient.getParking(parkingId);
        Availability availability = parkingClient.getAvailability(parkingId);

        BigDecimal hourly = PricingCalculator.hourlyPrice(
                parking.hourlyRate(), availability.occupancyRate(), alpha, minMultiplier, maxMultiplier);

        long minutes = Duration.between(start, end).toMinutes();
        long hours = (minutes + 59) / 60;
        BigDecimal total = hourly.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);

        return new PriceEstimate(parkingId, parking.hourlyRate(), availability.occupancyRate(),
                hourly, hours, total);
    }
}