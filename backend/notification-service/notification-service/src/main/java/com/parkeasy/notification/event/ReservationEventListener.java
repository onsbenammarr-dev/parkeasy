package com.parkeasy.notification.event;

import com.parkeasy.notification.config.RabbitConfig;
import com.parkeasy.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {

    private final NotificationService notificationService;

    public ReservationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handle(ReservationEvent event) {
        String message = switch (event.type()) {
            case "ReservationConfirmed" ->
                    "Votre réservation n°" + event.reservationId() + " est confirmée (" + event.amount() + " DT).";
            case "PaymentFailed" ->
                    "Le paiement de la réservation n°" + event.reservationId()
                            + " a échoué : la réservation a été annulée.";
            default -> null;
        };
        if (message != null) {
            notificationService.create(event.userId(), message);
        }
    }
}