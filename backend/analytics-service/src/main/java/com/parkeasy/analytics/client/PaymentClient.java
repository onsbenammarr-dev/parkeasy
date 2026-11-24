package com.parkeasy.analytics.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class PaymentClient {

    public record PaymentSummary(BigDecimal totalRevenue, long successfulPayments, long failedPayments) {
    }

    private final RestClient client;
    private final String internalKey;

    public PaymentClient(@Value("${payment.service.url}") String baseUrl,
                         @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalKey = internalKey;
    }

    /** Renvoie null si le Payment Service est indisponible : le tableau de bord reste affichable. */
    public PaymentSummary getSummary() {
        try {
            return client.get().uri("/api/internal/payments/summary")
                    .header("X-Internal-Key", internalKey)
                    .retrieve().body(PaymentSummary.class);
        } catch (RestClientException e) {
            return null;
        }
    }
}