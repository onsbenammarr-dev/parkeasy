package com.parkeasy.parking.service;

import com.parkeasy.parking.dto.ParkingRequest;
import com.parkeasy.parking.dto.ParkingResponse;
import com.parkeasy.parking.entity.Parking;
import com.parkeasy.parking.repository.ParkingRepository;
import com.parkeasy.parking.security.ParkingAccess;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final ParkingAccess access;

    public ParkingService(ParkingRepository parkingRepository, ParkingAccess access) {
        this.parkingRepository = parkingRepository;
        this.access = access;
    }

    public List<ParkingResponse> findAllActive() {
        return parkingRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    /** Un gestionnaire voit uniquement ses parkings ; l'administrateur voit tous les parkings actifs. */
    public List<ParkingResponse> mine(Authentication auth) {
        List<Parking> parkings = access.isAdmin(auth)
                ? parkingRepository.findByActiveTrue()
                : parkingRepository.findByManagerIdAndActiveTrue((Long) auth.getPrincipal());
        return parkings.stream().map(this::toResponse).toList();
    }

    public ParkingResponse findById(Long id) {
        return toResponse(parkingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable")));
    }

    public ParkingResponse create(Authentication auth, ParkingRequest request) {
        Parking parking = new Parking();
        parking.setManagerId((Long) auth.getPrincipal());
        apply(parking, request);
        return toResponse(parkingRepository.save(parking));
    }

    public ParkingResponse update(Authentication auth, Long id, ParkingRequest request) {
        Parking parking = access.requireManageable(auth, id);
        apply(parking, request);
        return toResponse(parkingRepository.save(parking));
    }

    public void deactivate(Authentication auth, Long id) {
        Parking parking = access.requireManageable(auth, id);
        parking.setActive(false);
        parkingRepository.save(parking);
    }

    private void apply(Parking p, ParkingRequest r) {
        p.setName(r.name());
        p.setAddress(r.address());
        p.setLatitude(r.latitude());
        p.setLongitude(r.longitude());
        p.setHourlyRate(r.hourlyRate());
    }

    private ParkingResponse toResponse(Parking p) {
        return new ParkingResponse(p.getId(), p.getName(), p.getAddress(),
                p.getLatitude(), p.getLongitude(), p.getHourlyRate(), p.getActive(), p.getManagerId());
    }
}