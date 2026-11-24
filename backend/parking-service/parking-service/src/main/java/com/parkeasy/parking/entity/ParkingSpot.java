package com.parkeasy.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"parking_id", "spot_number"}))
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parking_id")
    private Parking parking;

    @Column(name = "spot_number", nullable = false)
    private String spotNumber;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotStatus physicalStatus = SpotStatus.FREE;

    public Long getId() { return id; }
    public Parking getParking() { return parking; }
    public void setParking(Parking parking) { this.parking = parking; }
    public String getSpotNumber() { return spotNumber; }
    public void setSpotNumber(String spotNumber) { this.spotNumber = spotNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public SpotStatus getPhysicalStatus() { return physicalStatus; }
    public void setPhysicalStatus(SpotStatus physicalStatus) { this.physicalStatus = physicalStatus; }
}