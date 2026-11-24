package com.parkeasy.recommendation.service;

import com.parkeasy.recommendation.client.ParkingClient;
import com.parkeasy.recommendation.client.ParkingClient.Availability;
import com.parkeasy.recommendation.client.ParkingClient.ParkingInfo;
import com.parkeasy.recommendation.dto.RecommendationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RecommendationService {

    private record Candidate(ParkingInfo parking, Availability availability, double distanceKm) {
    }

    private final ParkingClient parkingClient;
    private final double maxDistanceKm;

    public RecommendationService(ParkingClient parkingClient,
                                 @Value("${recommendation.max-distance-km}") double maxDistanceKm) {
        this.parkingClient = parkingClient;
        this.maxDistanceKm = maxDistanceKm;
    }

    public List<RecommendationResponse> recommend(double lat, double lng, int limit) {
        List<ParkingInfo> parkings = parkingClient.getParkings();
        if (parkings.isEmpty()) {
            return List.of();
        }

        List<Candidate> candidates = parkings.stream()
                .map(p -> new Candidate(p, parkingClient.getAvailability(p.id()),
                        ScoreCalculator.haversineKm(lat, lng, p.latitude(), p.longitude())))
                .toList();

        double minPrice = candidates.stream().mapToDouble(c -> c.parking().hourlyRate().doubleValue()).min().orElse(0);
        double maxPrice = candidates.stream().mapToDouble(c -> c.parking().hourlyRate().doubleValue()).max().orElse(0);

        List<RecommendationResponse> results = new ArrayList<>();
        for (Candidate c : candidates) {
            Availability a = c.availability();
            long usable = a.totalSpots() - a.outOfServiceSpots();
            double occupancy = usable <= 0 ? 1.0 : (double) a.occupiedSpots() / usable;

            double d = ScoreCalculator.distanceScore(c.distanceKm(), maxDistanceKm);
            double p = ScoreCalculator.priceScore(c.parking().hourlyRate().doubleValue(), minPrice, maxPrice);
            double av = ScoreCalculator.availabilityScore(a.freeSpots(), a.totalSpots(), a.outOfServiceSpots());
            double t = ScoreCalculator.waitScore(occupancy);
            double score = Math.round(ScoreCalculator.score(d, p, av, t) * 10.0) / 10.0;

            results.add(new RecommendationResponse(
                    c.parking().id(), c.parking().name(), c.parking().address(),
                    Math.round(c.distanceKm() * 100.0) / 100.0,
                    c.parking().hourlyRate(), a.freeSpots(), score,
                    justify(c.parking().name(), d, p, av)));
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(RecommendationResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private String justify(String name, double d, double p, double a) {
        List<String> reasons = new ArrayList<>();
        if (d >= 70) {
            reasons.add("proche de votre destination");
        }
        if (p >= 60) {
            reasons.add("tarif raisonnable");
        }
        if (a >= 60) {
            reasons.add("bonne disponibilité");
        }
        if (reasons.isEmpty()) {
            return name + " : option disponible";
        }
        return name + " recommandé : " + String.join(", ", reasons);
    }
}