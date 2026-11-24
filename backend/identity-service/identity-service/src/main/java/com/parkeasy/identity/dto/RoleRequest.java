package com.parkeasy.identity.dto;

import com.parkeasy.identity.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(@NotNull Role role) {
}