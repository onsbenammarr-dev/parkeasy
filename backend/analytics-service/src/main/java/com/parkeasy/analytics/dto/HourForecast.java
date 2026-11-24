package com.parkeasy.analytics.dto;

import java.time.DayOfWeek;

public record HourForecast(
        DayOfWeek dayOfWeek,
        int hour,
        int samples,
        Double averageOccupancyPercent) {
}