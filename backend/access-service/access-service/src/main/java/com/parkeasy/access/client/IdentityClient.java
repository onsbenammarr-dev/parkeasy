package com.parkeasy.access.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class IdentityClient {

    public record UserInfo(Long id, String fullName, String email) {
    }

    private final RestClient client;
    private final String internalKey;

    public IdentityClient(@Value("${identity.service.url}") String baseUrl,
                          @Value("${internal.api-key}") String internalKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.internalKey = internalKey;
    }

    public Optional<UserInfo> findUser(Long id) {
        try {
            return Optional.ofNullable(client.get()
                    .uri("/api/internal/users/{id}", id)
                    .header("X-Internal-Key", internalKey)
                    .retrieve()
                    .body(UserInfo.class));
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }
}