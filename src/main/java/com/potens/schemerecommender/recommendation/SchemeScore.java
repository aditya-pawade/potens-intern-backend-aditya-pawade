package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.entity.Scheme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Immutable score for a single scheme after evaluation.
 * Contains total score, qualification status, and per-rule results.
 */
@Getter
@AllArgsConstructor
@Builder
public class SchemeScore {

    private final Scheme scheme;
    private final int totalScore;
    private final int maxPossibleScore;
    private final boolean qualified;
    private final List<RuleEvaluationResult> matchedResults;
    private final List<RuleEvaluationResult> unmatchedResults;

    /**
     * Score percentage for ranking. Primary sort key.
     * Returns 0.0 if maxPossibleScore is 0 (scheme has no rules).
     */
    public double getScorePercentage() {
        if (maxPossibleScore == 0) return 0.0;
        return (totalScore * 100.0) / maxPossibleScore;
    }
}
