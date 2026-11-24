package com.parkeasy.payment.event;

import com.parkeasy.payment.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /** Une panne de RabbitMQ ne doit jamais faire échouer un paiement déjà enregistré. */
    public void publish(String routingKey, ReservationEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        } catch (AmqpException e) {
            log.warn("Événement {} non publié : {}", event.type(), e.getMessage());
        }
    }
}