package com.parkeasy.parking.repository;

import com.parkeasy.parking.entity.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findByActiveTrue();
    List<Parking> findByManagerIdAndActiveTrue(Long managerId);
}