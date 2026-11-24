package com.parkeasy.reservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "spot_locks")
public class SpotLock {

    @Id
    private Long spotId;

    protected SpotLock() {
    }

    public SpotLock(Long spotId) {
        this.spotId = spotId;
    }

    public Long getSpotId() {
        return spotId;
    }
}