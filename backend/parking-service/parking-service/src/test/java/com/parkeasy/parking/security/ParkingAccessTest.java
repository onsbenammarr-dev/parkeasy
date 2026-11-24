package com.parkeasy.parking.security;

import com.parkeasy.parking.entity.Parking;
import com.parkeasy.parking.repository.ParkingRepository;
import com.parkeasy.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingAccessTest {

    @Mock
    ParkingRepository parkingRepository;
    @Mock
    ParkingSpotRepository spotRepository;
    @InjectMocks
    ParkingAccess access;

    private Authentication auth(Long userId, String role) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private Parking parkingOf(Long managerId) {
        Parking p = new Parking();
        p.setManagerId(managerId);
        return p;
    }

    @Test
    void leProprietairePeutGererSonParking() {
        Parking p = parkingOf(7L);
        when(parkingRepository.findById(5L)).thenReturn(Optional.of(p));
        assertSame(p, access.requireManageable(auth(7L, "MANAGER"), 5L));
    }

    @Test
    void unAutreGestionnaireEstRefuse() {
        when(parkingRepository.findById(5L)).thenReturn(Optional.of(parkingOf(7L)));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> access.requireManageable(auth(8L, "MANAGER"), 5L));
        assertEquals(403, e.getStatusCode().value());
    }

    @Test
    void lAdministrateurPeutToutGerer() {
        Parking p = parkingOf(7L);
        when(parkingRepository.findById(5L)).thenReturn(Optional.of(p));
        assertSame(p, access.requireManageable(auth(1L, "ADMIN"), 5L));
    }

    @Test
    void unParkingSansProprietaireEstReserveALAdministrateur() {
        when(parkingRepository.findById(5L)).thenReturn(Optional.of(parkingOf(null)));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> access.requireManageable(auth(7L, "MANAGER"), 5L));
        assertEquals(403, e.getStatusCode().value());
    }

    @Test
    void parkingInconnu() {
        when(parkingRepository.findById(5L)).thenReturn(Optional.empty());
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> access.requireManageable(auth(7L, "MANAGER"), 5L));
        assertEquals(404, e.getStatusCode().value());
    }
}