package com.parkeasy.payment.service;

import com.parkeasy.payment.client.ReservationClient;
import com.parkeasy.payment.client.ReservationClient.ReservationInfo;
import com.parkeasy.payment.dto.PaymentRequest;
import com.parkeasy.payment.dto.PaymentResponse;
import com.parkeasy.payment.entity.Payment;
import com.parkeasy.payment.entity.PaymentStatus;
import com.parkeasy.payment.event.EventPublisher;
import com.parkeasy.payment.event.ReservationEvent;
import com.parkeasy.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationClient reservationClient;
    private final EventPublisher eventPublisher;

    public PaymentService(PaymentRepository paymentRepository, ReservationClient reservationClient,
                          EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.reservationClient = reservationClient;
        this.eventPublisher = eventPublisher;
    }

    public PaymentResponse pay(Long userId, PaymentRequest request) {
        ReservationInfo reservation = reservationClient.get(request.reservationId());

        if (!reservation.userId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Réservation introuvable");
        }
        if (!"PENDING".equals(reservation.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette réservation n'est pas en attente de paiement");
        }

        boolean success = !request.simulateFailure();

        Payment payment = new Payment();
        payment.setReservationId(reservation.id());
        payment.setUserId(userId);
        payment.setAmount(reservation.totalPrice());
        payment.setMethod(request.method());
        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        if (success) {
            reservationClient.confirm(reservation.id());
            eventPublisher.publish("reservation.confirmed", new ReservationEvent(
                    UUID.randomUUID().toString(), "ReservationConfirmed", reservation.id(), userId,
                    payment.getAmount(), LocalDateTime.now().toString()));
        } else {
            reservationClient.cancel(reservation.id());
            eventPublisher.publish("payment.failed", new ReservationEvent(
                    UUID.randomUUID().toString(), "PaymentFailed", reservation.id(), userId,
                    payment.getAmount(), LocalDateTime.now().toString()));
        }
        return toResponse(payment);
    }

    public List<PaymentResponse> myPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByPaymentDateDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    private PaymentResponse toResponse(Payment p) {
        String invoice = p.getStatus() == PaymentStatus.SUCCESS
                ? String.format("INV-%d-%06d", p.getPaymentDate().getYear(), p.getId())
                : null;
        return new PaymentResponse(p.getId(), p.getReservationId(), p.getAmount(),
                p.getMethod(), p.getStatus().name(), p.getPaymentDate(), invoice);
    }
}