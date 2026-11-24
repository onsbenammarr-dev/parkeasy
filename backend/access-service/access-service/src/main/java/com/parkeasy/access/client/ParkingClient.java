package com.parkeasy.access.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class ParkingClient {

    public record ParkingInfo(Long id, String name, String address, Long managerId) {
    }

    public record SpotInfo(Long id, String spotNumber) {
    }

    private final RestClient client;

    public ParkingClient(@Value("${parking.service.url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Optional<ParkingInfo> findParking(Long id) {
        try {
            return Optional.ofNullable(client.get().uri("/api/parkings/{id}", id)
                    .retrieve().body(ParkingInfo.class));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    public Optional<String> findSpotNumber(Long parkingId, Long spotId) {
        try {
            List<SpotInfo> spots = client.get().uri("/api/parkings/{id}/spots", parkingId)
                    .retrieve().body(new ParameterizedTypeReference<List<SpotInfo>>() { });
            if (spots == null) {
                return Optional.empty();
            }
            return spots.stream().filter(s -> s.id().equals(spotId)).map(SpotInfo::spotNumber).findFirst();
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}