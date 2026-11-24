package com.parkeasy.parking.simulator;

import com.parkeasy.parking.dto.SensorEventRequest;
import com.parkeasy.parking.entity.ParkingSpot;
import com.parkeasy.parking.entity.SpotStatus;
import com.parkeasy.parking.repository.ParkingSpotRepository;
import com.parkeasy.parking.service.SensorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "parkeasy.simulator.enabled", havingValue = "true")
public class SensorSimulator {

    private final ParkingSpotRepository spotRepository;
    private final SensorService sensorService;
    private final Random random = new Random();

    public SensorSimulator(ParkingSpotRepository spotRepository, SensorService sensorService) {
        this.spotRepository = spotRepository;
        this.sensorService = sensorService;
    }

    @Scheduled(fixedDelayString = "${parkeasy.simulator.interval-ms:10000}")
    public void tick() {
        List<ParkingSpot> spots = spotRepository.findAll().stream()
                .filter(s -> s.getPhysicalStatus() != SpotStatus.OUT_OF_SERVICE)
                .toList();
        if (spots.isEmpty()) {
            return;
        }
        ParkingSpot spot = spots.get(random.nextInt(spots.size()));
        SpotStatus next = spot.getPhysicalStatus() == SpotStatus.FREE ? SpotStatus.OCCUPIED : SpotStatus.FREE;
        sensorService.processEvent(new SensorEventRequest(
                "SENSOR-" + spot.getId(), spot.getId(), next, LocalDateTime.now()));
    }
}