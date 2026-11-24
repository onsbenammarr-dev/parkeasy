package com.parkeasy.access.dto;

public record ScanResponse(String message, Long reservationId, String ticketStatus, TicketDetails details) {
}