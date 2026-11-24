package com.parkeasy.parking.service;

import com.parkeasy.parking.dto.SensorEventRequest;
import com.parkeasy.parking.dto.SpotResponse;
import com.parkeasy.parking.entity.ParkingSpot;
import com.parkeasy.parking.entity.Sensor;
import com.parkeasy.parking.repository.ParkingSpotRepository;
import com.parkeasy.parking.repository.SensorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final ParkingSpotRepository spotRepository;

    public SensorService(SensorRepository sensorRepository, ParkingSpotRepository spotRepository) {
        this.sensorRepository = sensorRepository;
        this.spotRepository = spotRepository;
    }

    @Transactional
    public SpotResponse processEvent(SensorEventRequest request) {
        ParkingSpot spot = spotRepository.findById(request.parkingSpotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place introuvable"));

        LocalDateTime time = request.timestamp() != null ? request.timestamp() : LocalDateTime.now();

        Sensor sensor = sensorRepository.findBySensorCode(request.sensorCode()).orElseGet(() -> {
            Sensor s = new Sensor();
            s.setSensorCode(request.sensorCode());
            s.setSpot(spot);
            return s;
        });

        if (!sensor.getSpot().getId().equals(spot.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce capteur est déjà lié à une autre place");
        }
        if (sensor.getLastUpdate() != null && time.isBefore(sensor.getLastUpdate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Événement plus ancien que la dernière mise à jour");
        }

        sensor.setState(request.status());
        sensor.setLastUpdate(time);
        sensorRepository.save(sensor);

        spot.setPhysicalStatus(request.status());
        spotRepository.save(spot);

        return new SpotResponse(spot.getId(), spot.getParking().getId(), spot.getSpotNumber(),
                spot.getType(), spot.getPhysicalStatus().name());
    }
}