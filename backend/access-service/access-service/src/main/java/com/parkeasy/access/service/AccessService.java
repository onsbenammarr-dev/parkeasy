package com.parkeasy.access.service;

import com.parkeasy.access.client.IdentityClient;
import com.parkeasy.access.client.IdentityClient.UserInfo;
import com.parkeasy.access.client.ParkingClient;
import com.parkeasy.access.client.ParkingClient.ParkingInfo;
import com.parkeasy.access.client.ReservationClient;
import com.parkeasy.access.client.ReservationClient.ReservationInfo;
import com.parkeasy.access.dto.ScanRequest;
import com.parkeasy.access.dto.ScanResponse;
import com.parkeasy.access.dto.TicketDetails;
import com.parkeasy.access.dto.TicketResponse;
import com.parkeasy.access.entity.AccessLog;
import com.parkeasy.access.entity.EventType;
import com.parkeasy.access.entity.QrTicket;
import com.parkeasy.access.entity.TicketStatus;
import com.parkeasy.access.repository.AccessLogRepository;
import com.parkeasy.access.repository.QrTicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccessService {

    private record Context(ReservationInfo reservation, Optional<ParkingInfo> parking, Optional<UserInfo> user) {
    }

    private final QrTicketRepository ticketRepository;
    private final AccessLogRepository logRepository;
    private final ReservationClient reservationClient;
    private final QrCodeGenerator qrCodeGenerator;
    private final IdentityClient identityClient;
    private final ParkingClient parkingClient;
    private final ScanWriter scanWriter;
    private final long earlyEntryMinutes;

    public AccessService(QrTicketRepository ticketRepository,
                         AccessLogRepository logRepository,
                         ReservationClient reservationClient,
                         QrCodeGenerator qrCodeGenerator,
                         IdentityClient identityClient,
                         ParkingClient parkingClient,
                         ScanWriter scanWriter,
                         @Value("${access.early-entry-minutes}") long earlyEntryMinutes) {
        this.ticketRepository = ticketRepository;
        this.logRepository = logRepository;
        this.reservationClient = reservationClient;
        this.qrCodeGenerator = qrCodeGenerator;
        this.identityClient = identityClient;
        this.parkingClient = parkingClient;
        this.scanWriter = scanWriter;
        this.earlyEntryMinutes = earlyEntryMinutes;
    }

    public TicketResponse createTicket(Long userId, Long reservationId) {
        ReservationInfo reservation = reservationClient.get(reservationId);
        if (!reservation.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable");
        }
        if (!"CONFIRMED".equals(reservation.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La réservation doit être confirmée (payée) pour obtenir un QR Code");
        }
        QrTicket ticket = ticketRepository.findByReservationId(reservationId).orElseGet(() -> {
            QrTicket t = new QrTicket();
            t.setReservationId(reservationId);
            t.setUserId(userId);
            t.setToken(UUID.randomUUID().toString());
            t.setValidFrom(reservation.startTime().minusMinutes(earlyEntryMinutes));
            t.setExpiration(reservation.endTime());
            t.setStatus(TicketStatus.ACTIVE);
            return ticketRepository.save(t);
        });
        return toResponse(ticket);
    }

    /** Fiche du ticket, sans rien modifier : à afficher avant de valider l'entrée ou la sortie. */
    public TicketDetails lookup(Authentication auth, String token) {
        QrTicket ticket = findTicket(token);
        return details(ticket, loadContext(auth, ticket));
    }

    public ScanResponse scan(Authentication auth, ScanRequest request) {
        QrTicket ticket = findTicket(request.token());
        Context context = loadContext(auth, ticket);

        if (request.eventType() == EventType.ENTRY && !"CONFIRMED".equals(context.reservation().status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Réservation non payée ou annulée : entrée refusée");
        }
        QrTicket updated = scanWriter.apply(request.token(), request.eventType());

        String message = request.eventType() == EventType.ENTRY ? "Entrée autorisée" : "Sortie enregistrée";
        return new ScanResponse(message, updated.getReservationId(), updated.getStatus().name(),
                details(updated, context));
    }

    private QrTicket findTicket(String token) {
        return ticketRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR Code invalide"));
    }

    /** Charge la réservation, le parking et le client, et vérifie que le gestionnaire est bien le propriétaire. */
    private Context loadContext(Authentication auth, QrTicket ticket) {
        ReservationInfo reservation = reservationClient.get(ticket.getReservationId());
        Optional<ParkingInfo> parking = reservation.parkingId() == null
                ? Optional.empty()
                : parkingClient.findParking(reservation.parkingId());

        if (!isAdmin(auth)) {
            if (parking.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Impossible de vérifier le parking de ce ticket. Réessaie dans un instant.");
            }
            Long managerId = parking.get().managerId();
            if (managerId == null || !managerId.equals((Long) auth.getPrincipal())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Ce ticket concerne un parking qui ne t'appartient pas");
            }
        }
        return new Context(reservation, parking, identityClient.findUser(reservation.userId()));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private TicketDetails details(QrTicket ticket, Context context) {
        ReservationInfo r = context.reservation();
        LocalDateTime now = LocalDateTime.now();
        boolean paid = "CONFIRMED".equals(r.status());

        long minutesToStart = now.isBefore(r.startTime()) ? Duration.between(now, r.startTime()).toMinutes() : 0;
        long minutesRemaining = Duration.between(now, r.endTime()).toMinutes();

        boolean canEnter = paid
                && ticket.getStatus() == TicketStatus.ACTIVE
                && !now.isAfter(ticket.getExpiration())
                && !now.isBefore(ticket.getValidFrom());
        boolean canExit = ticket.getStatus() == TicketStatus.IN_USE;

        LocalDateTime enteredAt = logRepository
                .findFirstByTicketIdAndEventTypeOrderByEventTimeDesc(ticket.getId(), EventType.ENTRY)
                .map(AccessLog::getEventTime).orElse(null);
        LocalDateTime exitedAt = logRepository
                .findFirstByTicketIdAndEventTypeOrderByEventTimeDesc(ticket.getId(), EventType.EXIT)
                .map(AccessLog::getEventTime).orElse(null);

        String spotNumber = (r.parkingId() != null && r.spotId() != null)
                ? parkingClient.findSpotNumber(r.parkingId(), r.spotId()).orElse("n°" + r.spotId())
                : "inconnue";

        return new TicketDetails(
                ticket.getReservationId(),
                ticket.getStatus().name(),
                context.user().map(UserInfo::fullName).orElse(null),
                context.user().map(UserInfo::email).orElse(null),
                context.parking().map(ParkingInfo::name).orElse(null),
                context.parking().map(ParkingInfo::address).orElse(null),
                spotNumber,
                r.startTime(),
                r.endTime(),
                paid,
                r.totalPrice(),
                enteredAt,
                exitedAt,
                minutesToStart,
                minutesRemaining,
                canEnter,
                canExit,
                verdict(ticket, paid, now));
    }

    private String verdict(QrTicket ticket, boolean paid, LocalDateTime now) {
        if (ticket.getStatus() == TicketStatus.IN_USE) {
            return now.isAfter(ticket.getExpiration())
                    ? "Véhicule dans le parking : la durée réservée est dépassée."
                    : "Véhicule dans le parking : sortie possible.";
        }
        if (ticket.getStatus() == TicketStatus.USED) {
            return "Ticket déjà utilisé : le véhicule est sorti.";
        }
        if (!paid) {
            return "Réservation non payée ou annulée : entrée refusée.";
        }
        if (now.isAfter(ticket.getExpiration())) {
            return "Ticket expiré : la réservation est terminée.";
        }
        if (now.isBefore(ticket.getValidFrom())) {
            return "Trop tôt : la réservation n'a pas encore commencé.";
        }
        return "Ticket valide : entrée autorisée.";
    }

    private TicketResponse toResponse(QrTicket t) {
        return new TicketResponse(t.getReservationId(), t.getToken(), t.getValidFrom(), t.getExpiration(),
                t.getStatus().name(), qrCodeGenerator.toBase64Png(t.getToken()));
    }
}