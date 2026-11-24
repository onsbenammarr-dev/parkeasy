package com.parkeasy.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Component
public class ReservationClient {

    public record ReservationInfo(Long id, Long userId, BigDecimal totalPrice, String status) {
    }

    private final RestClient client;
    private final String internalKey;

    public ReservationClient(@Value("${reservation.service.url}") String baseUrl,
                             @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalKey = internalKey;
    }

    public ReservationInfo get(Long id) {
        return send(HttpMethod.GET, "", id);
    }

    public ReservationInfo confirm(Long id) {
        return send(HttpMethod.PATCH, "/confirm", id);
    }

    public ReservationInfo cancel(Long id) {
        return send(HttpMethod.PATCH, "/cancel", id);
    }

    private ReservationInfo send(HttpMethod method, String suffix, Long id) {
        try {
            return client.method(method)
                    .uri("/api/internal/reservations/{id}" + suffix, id)
                    .header("X-Internal-Key", internalKey)
                    .retrieve()
                    .body(ReservationInfo.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(e.getStatusCode(), "Reservation Service : " + e.getStatusText());
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Reservation Service indisponible");
        }
    }
}