package com.parkeasy.reservation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PricingClient {

    public record Estimate(BigDecimal totalPrice) {
    }

    private final RestClient client;

    public PricingClient(@Value("${pricing.service.url}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Renvoie le prix dynamique, ou vide si le Pricing Service est indisponible. */
    public Optional<BigDecimal> estimateTotal(Long parkingId, LocalDateTime start, LocalDateTime end) {
        try {
            Estimate estimate = client.get()
                    .uri(builder -> builder.path("/api/pricing/estimate")
                            .queryParam("parkingId", parkingId)
                            .queryParam("start", start.toString())
                            .queryParam("end", end.toString())
                            .build())
                    .retrieve()
                    .body(Estimate.class);
            return Optional.ofNullable(estimate).map(Estimate::totalPrice);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}