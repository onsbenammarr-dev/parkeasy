package com.parkeasy.recommendation.controller;

import com.parkeasy.recommendation.dto.RecommendationResponse;
import com.parkeasy.recommendation.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationResponse> recommend(@RequestParam double lat,
                                                  @RequestParam double lng,
                                                  @RequestParam(defaultValue = "5") int limit) {
        return recommendationService.recommend(lat, lng, limit);
    }
}