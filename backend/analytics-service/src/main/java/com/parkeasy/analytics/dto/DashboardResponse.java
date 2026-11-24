package com.parkeasy.analytics.dto;

import com.parkeasy.analytics.client.PaymentClient.PaymentSummary;

import java.util.List;

public record DashboardResponse(
        List<ParkingStat> parkings,
        PaymentSummary payments,
        long snapshotsCount) {
}