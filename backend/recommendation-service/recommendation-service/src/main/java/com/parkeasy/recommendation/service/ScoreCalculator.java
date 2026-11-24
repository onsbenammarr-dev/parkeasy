package com.parkeasy.recommendation.service;

/**
 * S = 0.35 D + 0.30 P + 0.25 A + 0.10 T
 * Chaque critère est normalisé entre 0 et 100.
 */
public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static double score(double distance, double price, double availability, double wait) {
        return 0.35 * distance + 0.30 * price + 0.25 * availability + 0.10 * wait;
    }

    /** 100 si le parking est sur la destination, 0 à partir de maxKm. */
    public static double distanceScore(double km, double maxKm) {
        return 100.0 * (1.0 - Math.min(km, maxKm) / maxKm);
    }

    /** 100 pour le parking le moins cher, 0 pour le plus cher. */
    public static double priceScore(double price, double minPrice, double maxPrice) {
        if (maxPrice - minPrice < 1e-9) {
            return 100.0;
        }
        return 100.0 * (maxPrice - price) / (maxPrice - minPrice);
    }

    /** Pourcentage de places libres parmi les places en service. */
    public static double availabilityScore(long free, long total, long outOfService) {
        long usable = total - outOfService;
        return usable <= 0 ? 0.0 : 100.0 * free / usable;
    }

    /** Estimation simple du temps d'attente : plus le parking est plein, plus l'attente est longue. */
    public static double waitScore(double occupancy) {
        double o = Math.max(0.0, Math.min(1.0, occupancy));
        return 100.0 * (1.0 - o * o);
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }
}