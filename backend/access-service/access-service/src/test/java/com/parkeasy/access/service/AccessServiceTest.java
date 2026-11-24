package com.parkeasy.access.service;

import com.parkeasy.access.client.IdentityClient;
import com.parkeasy.access.client.IdentityClient.UserInfo;
import com.parkeasy.access.client.ParkingClient;
import com.parkeasy.access.client.ParkingClient.ParkingInfo;
import com.parkeasy.access.client.ReservationClient;
import com.parkeasy.access.client.ReservationClient.ReservationInfo;
import com.parkeasy.access.dto.ScanRequest;
import com.parkeasy.access.dto.TicketDetails;
import com.parkeasy.access.entity.EventType;
import com.parkeasy.access.entity.QrTicket;
import com.parkeasy.access.entity.TicketStatus;
import com.parkeasy.access.repository.AccessLogRepository;
import com.parkeasy.access.repository.QrTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    QrTicketRepository ticketRepository;
    @Mock
    AccessLogRepository logRepository;
    @Mock
    ReservationClient reservationClient;
    @Mock
    QrCodeGenerator qrCodeGenerator;
    @Mock
    IdentityClient identityClient;
    @Mock
    ParkingClient parkingClient;
    @Mock
    ScanWriter scanWriter;

    AccessService service;

    @BeforeEach
    void setUp() {
        service = new AccessService(ticketRepository, logRepository, reservationClient, qrCodeGenerator,
                identityClient, parkingClient, scanWriter, 15);
    }

    private Authentication auth(Long userId, String role) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private QrTicket validTicket() {
        QrTicket t = new QrTicket();
        t.setReservationId(7L);
        t.setUserId(1L);
        t.setToken("T");
        t.setValidFrom(LocalDateTime.now().minusMinutes(5));
        t.setExpiration(LocalDateTime.now().plusHours(1));
        t.setStatus(TicketStatus.ACTIVE);
        return t;
    }

    private ReservationInfo reservation(String status) {
        return new ReservationInfo(7L, 1L, new BigDecimal("5.00"), status,
                LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusHours(1), 3L, 30L);
    }

    private int statusCode(Executable action) {
        return assertThrows(ResponseStatusException.class, action).getStatusCode().value();
    }

    @Test
    void pasDeQrCodePourUneReservationNonPayee() {
        when(reservationClient.get(7L)).thenReturn(reservation("PENDING"));
        assertEquals(409, statusCode(() -> service.createTicket(1L, 7L)));
    }

    @Test
    void pasDeQrCodePourLaReservationDUnAutre() {
        when(reservationClient.get(7L)).thenReturn(reservation("CONFIRMED"));
        assertEquals(404, statusCode(() -> service.createTicket(2L, 7L)));
    }

    // Un gestionnaire ne scanne que les tickets de ses propres parkings
    @Test
    void unGestionnaireNePeutPasScannerLeTicketDUnAutreParking() {
        when(ticketRepository.findByToken("T")).thenReturn(Optional.of(validTicket()));
        when(reservationClient.get(7L)).thenReturn(reservation("CONFIRMED"));
        when(parkingClient.findParking(3L)).thenReturn(Optional.of(new ParkingInfo(3L, "Central", "Avenue", 99L)));

        int code = statusCode(() -> service.scan(auth(8L, "MANAGER"), new ScanRequest("T", EventType.ENTRY)));

        assertEquals(403, code);
        verify(scanWriter, never()).apply(any(), any());
    }

    @Test
    void entreeRefuseeSiLaReservationEstAnnulee() {
        when(ticketRepository.findByToken("T")).thenReturn(Optional.of(validTicket()));
        when(reservationClient.get(7L)).thenReturn(reservation("CANCELLED"));
        when(parkingClient.findParking(3L)).thenReturn(Optional.of(new ParkingInfo(3L, "Central", "Avenue", 99L)));
        when(identityClient.findUser(1L)).thenReturn(Optional.empty());

        int code = statusCode(() -> service.scan(auth(1L, "ADMIN"), new ScanRequest("T", EventType.ENTRY)));

        assertEquals(409, code);
        verify(scanWriter, never()).apply(any(), any());
    }

    @Test
    void laFicheAfficheClientParkingPaiementEtTempsRestant() {
        when(ticketRepository.findByToken("T")).thenReturn(Optional.of(validTicket()));
        when(reservationClient.get(7L)).thenReturn(reservation("CONFIRMED"));
        when(parkingClient.findParking(3L)).thenReturn(Optional.of(new ParkingInfo(3L, "Central", "Avenue Bourguiba", 7L)));
        when(parkingClient.findSpotNumber(3L, 30L)).thenReturn(Optional.of("A1"));
        when(identityClient.findUser(1L)).thenReturn(Optional.of(new UserInfo(1L, "Ons Ben Ammar", "ons@mail.com")));

        TicketDetails d = service.lookup(auth(7L, "MANAGER"), "T");

        assertEquals("Ons Ben Ammar", d.fullName());
        assertEquals("Central", d.parkingName());
        assertEquals("A1", d.spotNumber());
        assertTrue(d.paid());
        assertTrue(d.canEnter());
        assertFalse(d.canExit());
        assertTrue(d.minutesRemaining() >= 58 && d.minutesRemaining() <= 60);
    }
}