package com.parkeasy.access.service;

import com.parkeasy.access.entity.AccessLog;
import com.parkeasy.access.entity.EventType;
import com.parkeasy.access.entity.QrTicket;
import com.parkeasy.access.entity.TicketStatus;
import com.parkeasy.access.repository.AccessLogRepository;
import com.parkeasy.access.repository.QrTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanWriterTest {

    @Mock
    QrTicketRepository ticketRepository;
    @Mock
    AccessLogRepository logRepository;
    @InjectMocks
    ScanWriter writer;

    private QrTicket ticket(TicketStatus status, LocalDateTime validFrom, LocalDateTime expiration) {
        QrTicket t = new QrTicket();
        t.setReservationId(1L);
        t.setUserId(7L);
        t.setToken("T");
        t.setValidFrom(validFrom);
        t.setExpiration(expiration);
        t.setStatus(status);
        return t;
    }

    private QrTicket activeTicket() {
        return ticket(TicketStatus.ACTIVE, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
    }

    private int statusCode(Executable action) {
        return assertThrows(ResponseStatusException.class, action).getStatusCode().value();
    }

    @Test
    void entreeValideAutorisee() {
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(activeTicket()));
        when(ticketRepository.save(any(QrTicket.class))).thenAnswer(i -> i.getArgument(0));

        QrTicket result = writer.apply("T", EventType.ENTRY);

        assertEquals(TicketStatus.IN_USE, result.getStatus());
        verify(logRepository).save(any(AccessLog.class));
    }

    // Scénario 4 : un QR Code expiré n'autorise pas l'entrée
    @Test
    void qrCodeExpireRefuse() {
        QrTicket expired = ticket(TicketStatus.ACTIVE, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusMinutes(1));
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(expired));

        assertEquals(410, statusCode(() -> writer.apply("T", EventType.ENTRY)));
        verify(logRepository, never()).save(any());
    }

    // Scénario 4 : un QR Code déjà utilisé n'autorise pas une nouvelle entrée
    @Test
    void qrCodeDejaUtiliseRefuse() {
        QrTicket used = ticket(TicketStatus.IN_USE, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(used));

        assertEquals(409, statusCode(() -> writer.apply("T", EventType.ENTRY)));
        verify(logRepository, never()).save(any());
    }

    @Test
    void entreeTropTotRefusee() {
        QrTicket early = ticket(TicketStatus.ACTIVE, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3));
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(early));

        assertEquals(409, statusCode(() -> writer.apply("T", EventType.ENTRY)));
    }

    @Test
    void sortieSansEntreeRefusee() {
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(activeTicket()));

        assertEquals(409, statusCode(() -> writer.apply("T", EventType.EXIT)));
    }

    @Test
    void sortieApresEntreeAutorisee() {
        QrTicket inside = ticket(TicketStatus.IN_USE, LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.of(inside));
        when(ticketRepository.save(any(QrTicket.class))).thenAnswer(i -> i.getArgument(0));

        assertEquals(TicketStatus.USED, writer.apply("T", EventType.EXIT).getStatus());
    }

    @Test
    void qrCodeInconnuRefuse() {
        when(ticketRepository.findByTokenForUpdate("T")).thenReturn(Optional.empty());

        assertEquals(404, statusCode(() -> writer.apply("T", EventType.ENTRY)));
    }
}