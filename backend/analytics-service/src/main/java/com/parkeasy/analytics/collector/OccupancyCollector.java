package com.parkeasy.analytics.collector;

import com.parkeasy.analytics.client.ParkingClient;
import com.parkeasy.analytics.client.ParkingClient.Availability;
import com.parkeasy.analytics.client.ParkingClient.ParkingInfo;
import com.parkeasy.analytics.service.OccupancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "analytics.collector.enabled", havingValue = "true")
public class OccupancyCollector {

    private static final Logger log = LoggerFactory.getLogger(OccupancyCollector.class);

    private final ParkingClient parkingClient;
    private final OccupancyService occupancyService;

    public OccupancyCollector(ParkingClient parkingClient, OccupancyService occupancyService) {
        this.parkingClient = parkingClient;
        this.occupancyService = occupancyService;
    }

    @Scheduled(fixedDelayString = "${analytics.collector.interval-ms:60000}")
    public void collect() {
        try {
            for (ParkingInfo p : parkingClient.getParkings()) {
                Availability a = parkingClient.getAvailability(p.id());
                occupancyService.record(p.id(), a.occupancyRate(), LocalDateTime.now());
            }
        } catch (RuntimeException e) {
            log.warn("Collecte d'occupation impossible : {}", e.getMessage());
        }
    }
}