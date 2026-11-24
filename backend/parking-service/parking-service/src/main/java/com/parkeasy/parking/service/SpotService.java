package com.parkeasy.parking.service;

import com.parkeasy.parking.dto.AvailabilityResponse;
import com.parkeasy.parking.dto.SpotRequest;
import com.parkeasy.parking.dto.SpotResponse;
import com.parkeasy.parking.entity.Parking;
import com.parkeasy.parking.entity.ParkingSpot;
import com.parkeasy.parking.entity.SpotStatus;
import com.parkeasy.parking.repository.ParkingRepository;
import com.parkeasy.parking.repository.ParkingSpotRepository;
import com.parkeasy.parking.security.ParkingAccess;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SpotService {

    private final ParkingSpotRepository spotRepository;
    private final ParkingRepository parkingRepository;
    private final ParkingAccess access;

    public SpotService(ParkingSpotRepository spotRepository, ParkingRepository parkingRepository,
                       ParkingAccess access) {
        this.spotRepository = spotRepository;
        this.parkingRepository = parkingRepository;
        this.access = access;
    }

    public SpotResponse create(Authentication auth, SpotRequest request) {
        Parking parking = access.requireManageable(auth, request.parkingId());
        if (spotRepository.existsByParkingIdAndSpotNumber(parking.getId(), request.spotNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette place existe déjà dans ce parking");
        }
        ParkingSpot spot = new ParkingSpot();
        spot.setParking(parking);
        spot.setSpotNumber(request.spotNumber());
        spot.setType(request.type());
        return toResponse(spotRepository.save(spot));
    }

    public List<SpotResponse> findByParking(Long parkingId) {
        getParkingOrThrow(parkingId);
        return spotRepository.findByParkingId(parkingId).stream().map(this::toResponse).toList();
    }

    public SpotResponse updateStatus(Authentication auth, Long spotId, SpotStatus status) {
        ParkingSpot spot = access.requireManageableSpot(auth, spotId);
        spot.setPhysicalStatus(status);
        return toResponse(spotRepository.save(spot));
    }

    public AvailabilityResponse availability(Long parkingId) {
        getParkingOrThrow(parkingId);
        long free = spotRepository.countByParkingIdAndPhysicalStatus(parkingId, SpotStatus.FREE);
        long occupied = spotRepository.countByParkingIdAndPhysicalStatus(parkingId, SpotStatus.OCCUPIED);
        long out = spotRepository.countByParkingIdAndPhysicalStatus(parkingId, SpotStatus.OUT_OF_SERVICE);
        long total = free + occupied + out;
        long usable = total - out;
        double rate = usable == 0 ? 0.0 : Math.round((double) occupied / usable * 100.0) / 100.0;
        return new AvailabilityResponse(parkingId, total, free, occupied, out, rate);
    }

    private Parking getParkingOrThrow(Long id) {
        return parkingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable"));
    }

    private SpotResponse toResponse(ParkingSpot s) {
        return new SpotResponse(s.getId(), s.getParking().getId(), s.getSpotNumber(),
                s.getType(), s.getPhysicalStatus().name());
    }
}