package com.potens.schemerecommender.mapper;

import com.potens.schemerecommender.dto.response.RecommendationResponse;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.recommendation.RuleEvaluationResult;
import com.potens.schemerecommender.recommendation.SchemeScore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps SchemeScore (engine output) to RecommendationResponse (API output).
 */
public final class RecommendationMapper {

    private RecommendationMapper() {
        // Utility class
    }

    public static RecommendationResponse toResponse(SchemeScore score) {
        Scheme scheme = score.getScheme();

        List<String> matchedCriteria = score.getMatchedResults().stream()
                .map(RuleEvaluationResult::getExplanation)
                .collect(Collectors.toList());

        List<String> unmatchedCriteria = score.getUnmatchedResults().stream()
                .map(RuleEvaluationResult::getExplanation)
                .collect(Collectors.toList());

        String explanation = buildExplanationParagraph(scheme, score, matchedCriteria, unmatchedCriteria);

        return RecommendationResponse.builder()
                .schemeId(scheme.getId())
                .schemeName(scheme.getName())
                .schemeCategory(scheme.getCategory())
                .ministry(scheme.getMinistry())
                .benefitAmount(scheme.getBenefitAmount())
                .benefitDescription(scheme.getBenefitDescription())
                .totalScore(score.getTotalScore())
                .maxPossibleScore(score.getMaxPossibleScore())
                .matchedCriteria(matchedCriteria)
                .unmatchedCriteria(unmatchedCriteria)
                .explanation(explanation)
                .build();
    }

    private static String buildExplanationParagraph(
            Scheme scheme, SchemeScore score,
            List<String> matched, List<String> unmatched) {

        StringBuilder sb = new StringBuilder();

        String matchStrength = getMatchStrength(score.getScorePercentage());

        sb.append(String.format(
                "Based on your profile, you are a %s for %s (score: %d out of %d, %.0f%% match). ",
                matchStrength, scheme.getName(),
                score.getTotalScore(), score.getMaxPossibleScore(),
                score.getScorePercentage()));

        if (!matched.isEmpty()) {
            sb.append("You meet the following criteria: ");
            sb.append(String.join("; ", matched));
            sb.append(". ");
        }

        if (!unmatched.isEmpty()) {
            sb.append("Areas where you do not qualify: ");
            sb.append(String.join("; ", unmatched));
            sb.append(".");
        }

        return sb.toString();
    }

    /**
     * Varies explanation wording based on score percentage.
     */
    private static String getMatchStrength(double percentage) {
        if (percentage >= 80) {
            return "strong match";
        } else if (percentage >= 50) {
            return "good match";
        } else {
            return "partial match";
        }
    }
}
