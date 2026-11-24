package com.parkeasy.parking.controller;

import com.parkeasy.parking.dto.SensorEventRequest;
import com.parkeasy.parking.dto.SpotResponse;
import com.parkeasy.parking.security.ParkingAccess;
import com.parkeasy.parking.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensorService;
    private final ParkingAccess access;

    public SensorController(SensorService sensorService, ParkingAccess access) {
        this.sensorService = sensorService;
        this.access = access;
    }

    @PostMapping("/events")
    public SpotResponse receiveEvent(Authentication auth, @Valid @RequestBody SensorEventRequest request) {
        access.requireManageableSpot(auth, request.parkingSpotId());
        return sensorService.processEvent(request);
    }
}