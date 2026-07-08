package com.potens.schemerecommender.service;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.dto.response.RecommendationResponse;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.mapper.RecommendationMapper;
import com.potens.schemerecommender.recommendation.RecommendationEngine;
import com.potens.schemerecommender.recommendation.SchemeScore;
import com.potens.schemerecommender.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final SchemeRepository schemeRepository;
    private final RecommendationEngine recommendationEngine;
    private final int minScorePercentage;

    public RecommendationService(
            SchemeRepository schemeRepository,
            RecommendationEngine recommendationEngine,
            @Value("${recommendation.min-score-percentage:20}") int minScorePercentage) {
        this.schemeRepository = schemeRepository;
        this.recommendationEngine = recommendationEngine;
        this.minScorePercentage = minScorePercentage;
    }

    public List<RecommendationResponse> recommend(ProfileRequest profile) {
        List<Scheme> activeSchemes = schemeRepository.findAllActiveWithRules();

        if (activeSchemes.isEmpty()) {
            log.info("No active schemes found in the database");
            return Collections.emptyList();
        }

        List<SchemeScore> scores = recommendationEngine.evaluate(profile, activeSchemes);

        List<RecommendationResponse> recommendations = scores.stream()
                .filter(SchemeScore::isQualified)
                .filter(this::meetsMinimumScore)
                .sorted(
                    Comparator.comparingDouble(SchemeScore::getScorePercentage)
                              .reversed()
                              .thenComparing(
                                  Comparator.comparingInt(SchemeScore::getTotalScore).reversed()
                              )
                )
                .limit(3)
                .map(RecommendationMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Generated {} recommendations for profile", recommendations.size());
        return recommendations;
    }

    private boolean meetsMinimumScore(SchemeScore score) {
        return score.getScorePercentage() >= minScorePercentage;
    }
}
