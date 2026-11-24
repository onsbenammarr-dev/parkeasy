package com.parkeasy.analytics.repository;

import com.parkeasy.analytics.entity.OccupancySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OccupancySnapshotRepository extends JpaRepository<OccupancySnapshot, Long> {
    List<OccupancySnapshot> findByParkingIdAndRecordedAtAfter(Long parkingId, LocalDateTime after);
}