package com.parkeasy.identity.service;

import com.parkeasy.identity.client.NotificationClient;
import com.parkeasy.identity.dto.AuthResponse;
import com.parkeasy.identity.dto.LoginRequest;
import com.parkeasy.identity.dto.RegisterRequest;
import com.parkeasy.identity.dto.RegisterResponse;
import com.parkeasy.identity.dto.UserResponse;
import com.parkeasy.identity.entity.Role;
import com.parkeasy.identity.entity.User;
import com.parkeasy.identity.repository.UserRepository;
import com.parkeasy.identity.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationClient notificationClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificationClient = notificationClient;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        NotificationClient.WelcomeResult mail = notificationClient.sendWelcome(saved);
        return new RegisterResponse(saved.getId(), saved.getFullName(), saved.getEmail(),
                saved.getRole().name(), mail.emailSent(), mail.error());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        return new AuthResponse(jwtService.generateToken(user), user.getRole().name());
    }

    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return toResponse(user);
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toResponse)
                .toList();
    }

    public UserResponse changeRole(String adminEmail, Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (user.getEmail().equals(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tu ne peux pas changer ton propre rôle");
        }
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name());
    }
}