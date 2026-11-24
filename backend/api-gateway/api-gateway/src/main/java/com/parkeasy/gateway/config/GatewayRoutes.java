package com.parkeasy.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutes {

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${services.identity.url}") String identity,
            @Value("${services.parking.url}") String parking,
            @Value("${services.reservation.url}") String reservation,
            @Value("${services.payment.url}") String payment,
            @Value("${services.recommendation.url}") String recommendation,
            @Value("${services.pricing.url}") String pricing,
            @Value("${services.access.url}") String access,
            @Value("${services.notification.url}") String notification,
            @Value("${services.analytics.url}") String analytics) {

        return builder.routes()
                .route("identity", r -> r.path("/api/auth/**", "/api/users/**").uri(identity))
                .route("parking", r -> r.path("/api/parkings/**", "/api/spots/**", "/api/sensors/**").uri(parking))
                .route("reservation", r -> r.path("/api/reservations/**").uri(reservation))
                .route("payment", r -> r.path("/api/payments/**").uri(payment))
                .route("recommendation", r -> r.path("/api/recommendations/**").uri(recommendation))
                .route("pricing", r -> r.path("/api/pricing/**").uri(pricing))
                .route("access", r -> r.path("/api/access/**").uri(access))
                .route("notification", r -> r.path("/api/notifications/**").uri(notification))
                .route("analytics", r -> r.path("/api/analytics/**").uri(analytics))
                .build();
    }
}