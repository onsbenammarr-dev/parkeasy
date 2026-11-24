package com.parkeasy.identity.client;

import com.parkeasy.identity.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificationClient {

    public record WelcomeResult(boolean emailSent, String error) {
    }

    public record WelcomeBody(Long userId, String email, String fullName) {
    }

    private final RestClient client;
    private final String internalKey;

    public NotificationClient(@Value("${notification.service.url}") String baseUrl,
                              @Value("${internal.api-key}") String internalKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(25000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.internalKey = internalKey;
    }

    /** Ne lève jamais d'exception : un email raté ne doit pas empêcher la création du compte. */
    public WelcomeResult sendWelcome(User user) {
        try {
            WelcomeResult result = client.post()
                    .uri("/api/internal/emails/welcome")
                    .header("X-Internal-Key", internalKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new WelcomeBody(user.getId(), user.getEmail(), user.getFullName()))
                    .retrieve()
                    .body(WelcomeResult.class);
            return result != null ? result : new WelcomeResult(false, "Réponse vide du service de notification.");
        } catch (RestClientException e) {
            return new WelcomeResult(false, "Service de notification indisponible.");
        }
    }
}