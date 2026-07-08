package com.potens.schemerecommender.service;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.dto.response.RecommendationResponse;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.Category;
import com.potens.schemerecommender.enums.EducationLevel;
import com.potens.schemerecommender.enums.Gender;
import com.potens.schemerecommender.enums.Occupation;
import com.potens.schemerecommender.enums.ProfileAttribute;
import com.potens.schemerecommender.enums.RuleType;
import com.potens.schemerecommender.recommendation.RecommendationEngine;
import com.potens.schemerecommender.recommendation.RuleEvaluationResult;
import com.potens.schemerecommender.recommendation.RuleEvaluator;
import com.potens.schemerecommender.recommendation.SchemeScore;
import com.potens.schemerecommender.repository.SchemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private SchemeRepository schemeRepository;

    @Mock
    private RecommendationEngine recommendationEngine;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(schemeRepository, recommendationEngine, 20);
    }

    @Test
    @DisplayName("No active schemes should return empty recommendations")
    void noActiveSchemes_returnsEmptyList() {
        when(schemeRepository.findAllActiveWithRules()).thenReturn(Collections.emptyList());

        List<RecommendationResponse> result = recommendationService.recommend(buildProfile());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Qualified schemes should be sorted by score percentage descending")
    void qualifiedSchemes_sortedByPercentage() {
        Scheme scheme1 = buildScheme(1L, "Scheme A");
        Scheme scheme2 = buildScheme(2L, "Scheme B");
        when(schemeRepository.findAllActiveWithRules()).thenReturn(Arrays.asList(scheme1, scheme2));

        SchemeScore lowScore = SchemeScore.builder()
                .scheme(scheme1).totalScore(30).maxPossibleScore(100)
                .qualified(true)
                .matchedResults(buildResults(true))
                .unmatchedResults(Collections.emptyList())
                .build();
        SchemeScore highScore = SchemeScore.builder()
                .scheme(scheme2).totalScore(80).maxPossibleScore(100)
                .qualified(true)
                .matchedResults(buildResults(true))
                .unmatchedResults(Collections.emptyList())
                .build();

        when(recommendationEngine.evaluate(any(), anyList()))
                .thenReturn(Arrays.asList(lowScore, highScore));

        List<RecommendationResponse> result = recommendationService.recommend(buildProfile());

        assertEquals(2, result.size());
        assertEquals("Scheme B", result.get(0).getSchemeName());
        assertEquals("Scheme A", result.get(1).getSchemeName());
    }

    @Test
    @DisplayName("Should return at most 3 recommendations even if more qualify")
    void moreThanThreeQualified_returnsOnlyTopThree() {
        List<Scheme> schemes = new ArrayList<>();
        List<SchemeScore> scores = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Scheme s = buildScheme((long) i, "Scheme " + i);
            schemes.add(s);
            scores.add(SchemeScore.builder()
                    .scheme(s).totalScore(50 + i * 10).maxPossibleScore(100)
                    .qualified(true)
                    .matchedResults(buildResults(true))
                    .unmatchedResults(Collections.emptyList())
                    .build());
        }

        when(schemeRepository.findAllActiveWithRules()).thenReturn(schemes);
        when(recommendationEngine.evaluate(any(), anyList())).thenReturn(scores);

        List<RecommendationResponse> result = recommendationService.recommend(buildProfile());

        assertEquals(3, result.size());
    }

    // ──────────────── Helpers ────────────────

    private ProfileRequest buildProfile() {
        return ProfileRequest.builder()
                .age(25).annualIncome(300000.0).gender(Gender.MALE)
                .category(Category.GENERAL).state("MAHARASHTRA")
                .occupation(Occupation.FARMER).isRural(true)
                .hasDisability(false).educationLevel(EducationLevel.GRADUATE)
                .familySize(4).build();
    }

    private Scheme buildScheme(Long id, String name) {
        return Scheme.builder().id(id).name(name).category("Test")
                .ministry("Test").description("Test").isActive(true).build();
    }

    private List<RuleEvaluationResult> buildResults(boolean matched) {
        List<RuleEvaluationResult> results = new ArrayList<>();
        results.add(new RuleEvaluationResult(matched, matched ? 10 : 0,
                "Test explanation", false, ProfileAttribute.AGE, RuleType.MIN_VALUE));
        return results;
    }
}
