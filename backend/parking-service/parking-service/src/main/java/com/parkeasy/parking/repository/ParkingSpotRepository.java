package com.parkeasy.parking.repository;

import com.parkeasy.parking.entity.ParkingSpot;
import com.parkeasy.parking.entity.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByParkingId(Long parkingId);
    boolean existsByParkingIdAndSpotNumber(Long parkingId, String spotNumber);
    long countByParkingIdAndPhysicalStatus(Long parkingId, SpotStatus status);
}