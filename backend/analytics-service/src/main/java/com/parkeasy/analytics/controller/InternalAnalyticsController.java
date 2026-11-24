package com.parkeasy.analytics.controller;

import com.parkeasy.analytics.dto.SnapshotRequest;
import com.parkeasy.analytics.service.OccupancyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/analytics")
public class InternalAnalyticsController {

    private final OccupancyService occupancyService;
    private final String internalKey;

    public InternalAnalyticsController(OccupancyService occupancyService,
                                       @Value("${internal.api-key}") String internalKey) {
        this.occupancyService = occupancyService;
        this.internalKey = internalKey;
    }

    @PostMapping("/occupancy")
    @ResponseStatus(HttpStatus.CREATED)
    public void record(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                       @Valid @RequestBody SnapshotRequest request) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clé interne invalide");
        }
        occupancyService.record(request.parkingId(), request.occupancyRate(), request.recordedAt());
    }
}