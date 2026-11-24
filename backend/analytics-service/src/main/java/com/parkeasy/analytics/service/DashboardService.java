package com.parkeasy.analytics.service;

import com.parkeasy.analytics.client.ParkingClient;
import com.parkeasy.analytics.client.ParkingClient.Availability;
import com.parkeasy.analytics.client.PaymentClient;
import com.parkeasy.analytics.dto.DashboardResponse;
import com.parkeasy.analytics.dto.ParkingStat;
import com.parkeasy.analytics.repository.OccupancySnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final ParkingClient parkingClient;
    private final PaymentClient paymentClient;
    private final OccupancySnapshotRepository snapshotRepository;

    public DashboardService(ParkingClient parkingClient, PaymentClient paymentClient,
                            OccupancySnapshotRepository snapshotRepository) {
        this.parkingClient = parkingClient;
        this.paymentClient = paymentClient;
        this.snapshotRepository = snapshotRepository;
    }

    public DashboardResponse dashboard() {
        List<ParkingStat> parkings = parkingClient.getParkings().stream().map(p -> {
            Availability a = parkingClient.getAvailability(p.id());
            return new ParkingStat(p.id(), p.name(), a.totalSpots(), a.freeSpots(), a.occupancyRate());
        }).toList();
        return new DashboardResponse(parkings, paymentClient.getSummary(), snapshotRepository.count());
    }
}