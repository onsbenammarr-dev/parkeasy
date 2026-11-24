package com.parkeasy.notification.dto;

public record NotificationResult(boolean emailSent, String error) {
}