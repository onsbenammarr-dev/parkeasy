package com.parkeasy.parking.controller;

import com.parkeasy.parking.dto.AvailabilityResponse;
import com.parkeasy.parking.dto.SpotRequest;
import com.parkeasy.parking.dto.SpotResponse;
import com.parkeasy.parking.dto.StatusRequest;
import com.parkeasy.parking.service.SpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SpotController {

    private final SpotService spotService;

    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    @PostMapping("/spots")
    @ResponseStatus(HttpStatus.CREATED)
    public SpotResponse create(Authentication auth, @Valid @RequestBody SpotRequest request) {
        return spotService.create(auth, request);
    }

    @GetMapping("/parkings/{parkingId}/spots")
    public List<SpotResponse> list(@PathVariable Long parkingId) {
        return spotService.findByParking(parkingId);
    }

    @PatchMapping("/spots/{spotId}/status")
    public SpotResponse updateStatus(Authentication auth, @PathVariable Long spotId,
                                     @Valid @RequestBody StatusRequest request) {
        return spotService.updateStatus(auth, spotId, request.status());
    }

    @GetMapping("/parkings/{parkingId}/availability")
    public AvailabilityResponse availability(@PathVariable Long parkingId) {
        return spotService.availability(parkingId);
    }
}