package com.parkeasy.analytics.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

@Component
public class ParkingClient {

    public record ParkingInfo(Long id, String name) {
    }

    public record Availability(Long parkingId, long totalSpots, long freeSpots,
                               long occupiedSpots, long outOfServiceSpots, double occupancyRate) {
    }

    private final RestClient client;

    public ParkingClient(@Value("${parking.service.url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<ParkingInfo> getParkings() {
        return call(() -> client.get().uri("/api/parkings")
                .retrieve().body(new ParameterizedTypeReference<List<ParkingInfo>>() { }));
    }

    public Availability getAvailability(Long parkingId) {
        return call(() -> client.get().uri("/api/parkings/{id}/availability", parkingId)
                .retrieve().body(Availability.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Parking Service indisponible");
        }
    }
}