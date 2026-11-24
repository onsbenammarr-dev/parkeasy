package com.parkeasy.parking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sensorCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotStatus state;

    private LocalDateTime lastUpdate;

    public Long getId() { return id; }
    public String getSensorCode() { return sensorCode; }
    public void setSensorCode(String sensorCode) { this.sensorCode = sensorCode; }
    public ParkingSpot getSpot() { return spot; }
    public void setSpot(ParkingSpot spot) { this.spot = spot; }
    public SpotStatus getState() { return state; }
    public void setState(SpotStatus state) { this.state = state; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}