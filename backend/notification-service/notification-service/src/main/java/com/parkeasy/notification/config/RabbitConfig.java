package com.parkeasy.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "parkeasy.events";
    public static final String QUEUE = "notification.queue";

    @Bean
    public TopicExchange parkeasyExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding confirmedBinding(Queue notificationQueue, TopicExchange parkeasyExchange) {
        return BindingBuilder.bind(notificationQueue).to(parkeasyExchange).with("reservation.confirmed");
    }

    @Bean
    public Binding failedBinding(Queue notificationQueue, TopicExchange parkeasyExchange) {
        return BindingBuilder.bind(notificationQueue).to(parkeasyExchange).with("payment.failed");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}