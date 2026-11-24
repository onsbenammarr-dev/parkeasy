package com.parkeasy.analytics.service;

import com.parkeasy.analytics.dto.HourForecast;
import com.parkeasy.analytics.entity.OccupancySnapshot;
import com.parkeasy.analytics.repository.OccupancySnapshotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OccupancyService {

    private final OccupancySnapshotRepository repository;
    private final long historyWeeks;

    public OccupancyService(OccupancySnapshotRepository repository,
                            @Value("${analytics.history-weeks}") long historyWeeks) {
        this.repository = repository;
        this.historyWeeks = historyWeeks;
    }

    public void record(Long parkingId, double occupancyRate, LocalDateTime recordedAt) {
        OccupancySnapshot s = new OccupancySnapshot();
        s.setParkingId(parkingId);
        s.setOccupancyRate(occupancyRate);
        s.setRecordedAt(recordedAt != null ? recordedAt : LocalDateTime.now());
        repository.save(s);
    }

    public List<HourForecast> forecastNextHours(Long parkingId, int hours) {
        List<OccupancySnapshot> history = loadHistory(parkingId);
        LocalDateTime now = LocalDateTime.now();
        List<HourForecast> result = new ArrayList<>();
        for (int i = 0; i < hours; i++) {
            LocalDateTime t = now.plusHours(i);
            result.add(forecast(history, t.getDayOfWeek(), t.getHour()));
        }
        return result;
    }

    public HourForecast forecastAt(Long parkingId, DayOfWeek day, int hour) {
        return forecast(loadHistory(parkingId), day, hour);
    }

    private List<OccupancySnapshot> loadHistory(Long parkingId) {
        return repository.findByParkingIdAndRecordedAtAfter(parkingId, LocalDateTime.now().minusWeeks(historyWeeks));
    }

    /** Moyenne historique du taux d'occupation pour un jour de semaine et une heure donnés. */
    private HourForecast forecast(List<OccupancySnapshot> history, DayOfWeek day, int hour) {
        List<OccupancySnapshot> matching = history.stream()
                .filter(s -> s.getRecordedAt().getDayOfWeek() == day && s.getRecordedAt().getHour() == hour)
                .toList();
        if (matching.isEmpty()) {
            return new HourForecast(day, hour, 0, null);
        }
        double average = matching.stream().mapToDouble(OccupancySnapshot::getOccupancyRate).average().orElse(0);
        return new HourForecast(day, hour, matching.size(), Math.round(average * 1000.0) / 10.0);
    }
}