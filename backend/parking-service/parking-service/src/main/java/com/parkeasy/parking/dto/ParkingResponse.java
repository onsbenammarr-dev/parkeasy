package com.parkeasy.parking.dto;

import java.math.BigDecimal;

public record ParkingResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        BigDecimal hourlyRate,
        Boolean active,
        Long managerId) {
}