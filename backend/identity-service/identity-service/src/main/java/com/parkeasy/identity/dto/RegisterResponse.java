package com.parkeasy.identity.dto;

public record RegisterResponse(
        Long id,
        String fullName,
        String email,
        String role,
        boolean welcomeEmailSent,
        String emailError) {
}