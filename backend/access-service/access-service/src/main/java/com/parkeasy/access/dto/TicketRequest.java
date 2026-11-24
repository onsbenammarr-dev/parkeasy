package com.parkeasy.access.dto;

import jakarta.validation.constraints.NotNull;

public record TicketRequest(@NotNull Long reservationId) {
}