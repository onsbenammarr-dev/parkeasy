package com.parkeasy.access.service;

import com.parkeasy.access.entity.AccessLog;
import com.parkeasy.access.entity.EventType;
import com.parkeasy.access.entity.QrTicket;
import com.parkeasy.access.entity.TicketStatus;
import com.parkeasy.access.repository.AccessLogRepository;
import com.parkeasy.access.repository.QrTicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ScanWriter {

    private final QrTicketRepository ticketRepository;
    private final AccessLogRepository logRepository;

    public ScanWriter(QrTicketRepository ticketRepository, AccessLogRepository logRepository) {
        this.ticketRepository = ticketRepository;
        this.logRepository = logRepository;
    }

    @Transactional
    public QrTicket apply(String token, EventType type) {
        QrTicket ticket = ticketRepository.findByTokenForUpdate(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code invalide"));
        LocalDateTime now = LocalDateTime.now();

        if (type == EventType.ENTRY) {
            if (now.isAfter(ticket.getExpiration())) {
                throw new ResponseStatusException(HttpStatus.GONE, "QR Code expiré");
            }
            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QR Code déjà utilisé");
            }
            if (now.isBefore(ticket.getValidFrom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Trop tôt : la réservation n'a pas commencé");
            }
            ticket.setStatus(TicketStatus.IN_USE);
        } else {
            if (ticket.getStatus() != TicketStatus.IN_USE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sortie impossible : aucune entrée en cours");
            }
            ticket.setStatus(TicketStatus.USED);
        }
        QrTicket saved = ticketRepository.save(ticket);

        AccessLog log = new AccessLog();
        log.setTicketId(saved.getId());
        log.setEventType(type);
        log.setEventTime(now);
        logRepository.save(log);
        return saved;
    }
}