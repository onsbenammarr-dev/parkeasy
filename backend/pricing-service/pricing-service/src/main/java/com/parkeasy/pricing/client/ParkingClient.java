package com.parkeasy.pricing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.function.Supplier;

@Component
public class ParkingClient {

    public record ParkingInfo(Long id, BigDecimal hourlyRate, Boolean active) {
    }

    public record Availability(Long parkingId, double occupancyRate) {
    }

    private final RestClient client;

    public ParkingClient(@Value("${parking.service.url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ParkingInfo getParking(Long id) {
        return call(() -> client.get().uri("/api/parkings/{id}", id)
                .retrieve().body(ParkingInfo.class));
    }

    public Availability getAvailability(Long id) {
        return call(() -> client.get().uri("/api/parkings/{id}/availability", id)
                .retrieve().body(Availability.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Parking Service indisponible");
        }
    }
}