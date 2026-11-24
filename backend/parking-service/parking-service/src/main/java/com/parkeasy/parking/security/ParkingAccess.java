package com.parkeasy.parking.security;

import com.parkeasy.parking.entity.Parking;
import com.parkeasy.parking.entity.ParkingSpot;
import com.parkeasy.parking.repository.ParkingRepository;
import com.parkeasy.parking.repository.ParkingSpotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Règle unique de propriété : seul le gestionnaire propriétaire (ou l'administrateur) peut gérer un parking. */
@Component
public class ParkingAccess {

    private final ParkingRepository parkingRepository;
    private final ParkingSpotRepository spotRepository;

    public ParkingAccess(ParkingRepository parkingRepository, ParkingSpotRepository spotRepository) {
        this.parkingRepository = parkingRepository;
        this.spotRepository = spotRepository;
    }

    public boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Parking requireManageable(Authentication auth, Long parkingId) {
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking introuvable"));
        assertCanManage(auth, parking);
        return parking;
    }

    public ParkingSpot requireManageableSpot(Authentication auth, Long spotId) {
        ParkingSpot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place introuvable"));
        assertCanManage(auth, spot.getParking());
        return spot;
    }

    private void assertCanManage(Authentication auth, Parking parking) {
        if (isAdmin(auth)) {
            return;
        }
        Long userId = (Long) auth.getPrincipal();
        if (parking.getManagerId() == null || !parking.getManagerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce parking ne t'appartient pas");
        }
    }
}