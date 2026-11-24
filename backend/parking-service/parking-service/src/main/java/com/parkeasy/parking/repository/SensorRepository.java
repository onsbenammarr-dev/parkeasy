package com.parkeasy.parking.repository;

import com.parkeasy.parking.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySensorCode(String sensorCode);
}