package com.parkeasy.parking.dto;

import com.parkeasy.parking.entity.SpotStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull SpotStatus status) {
}