package com.parkeasy.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_history")
public class OccupancySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long parkingId;

    @Column(nullable = false)
    private Double occupancyRate;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    public Long getId() { return id; }
    public Long getParkingId() { return parkingId; }
    public void setParkingId(Long parkingId) { this.parkingId = parkingId; }
    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}