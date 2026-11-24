package com.parkeasy.identity.controller;

import com.parkeasy.identity.entity.User;
import com.parkeasy.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    public record InternalUser(Long id, String fullName, String email) {
    }

    private final UserRepository repository;
    private final String internalKey;

    public InternalUserController(UserRepository repository, @Value("${internal.api-key}") String internalKey) {
        this.repository = repository;
        this.internalKey = internalKey;
    }

    @GetMapping("/{id}")
    public InternalUser get(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                            @PathVariable Long id) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return new InternalUser(user.getId(), user.getFullName(), user.getEmail());
    }
}