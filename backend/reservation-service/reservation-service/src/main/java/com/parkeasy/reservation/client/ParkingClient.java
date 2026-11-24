package com.parkeasy.reservation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ParkingClient {

    public record ParkingInfo(Long id, String name, BigDecimal hourlyRate, Boolean active) {
    }

    public record SpotInfo(Long id, String spotNumber, String physicalStatus) {
    }

    private final RestClient client;

    public ParkingClient(@Value("${parking.service.url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ParkingInfo getParking(Long id) {
        try {
            return client.get().uri("/api/parkings/{id}", id)
                    .retrieve().body(ParkingInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Parking Service indisponible");
        }
    }

    public List<SpotInfo> getSpots(Long parkingId) {
        try {
            return client.get().uri("/api/parkings/{id}/spots", parkingId)
                    .retrieve().body(new ParameterizedTypeReference<List<SpotInfo>>() { });
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable");
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Parking Service indisponible");
        }
    }
}