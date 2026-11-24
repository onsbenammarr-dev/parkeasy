package com.parkeasy.analytics.controller;

import com.parkeasy.analytics.dto.DashboardResponse;
import com.parkeasy.analytics.dto.HourForecast;
import com.parkeasy.analytics.service.DashboardService;
import com.parkeasy.analytics.service.OccupancyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final OccupancyService occupancyService;
    private final DashboardService dashboardService;

    public AnalyticsController(OccupancyService occupancyService, DashboardService dashboardService) {
        this.occupancyService = occupancyService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/occupancy/{parkingId}/forecast")
    public List<HourForecast> forecast(@PathVariable Long parkingId,
                                       @RequestParam(defaultValue = "6") int hours) {
        if (hours < 1 || hours > 48) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hours doit être entre 1 et 48");
        }
        return occupancyService.forecastNextHours(parkingId, hours);
    }

    @GetMapping("/occupancy/{parkingId}/forecast/at")
    public HourForecast forecastAt(@PathVariable Long parkingId,
                                   @RequestParam DayOfWeek day,
                                   @RequestParam int hour) {
        if (hour < 0 || hour > 23) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hour doit être entre 0 et 23");
        }
        return occupancyService.forecastAt(parkingId, day, hour);
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return dashboardService.dashboard();
    }
}