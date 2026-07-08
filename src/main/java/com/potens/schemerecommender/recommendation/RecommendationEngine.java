package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.entity.SchemeRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a profile against all provided schemes.
 * Delegates per-rule evaluation to {@link RuleEvaluator}.
 */
@Component
public class RecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEngine.class);

    private final RuleEvaluator ruleEvaluator;

    public RecommendationEngine(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    /**
     * Evaluates a profile against all schemes.
     * Returns a SchemeScore for every scheme — caller handles filtering/sorting.
     *
     * @param profile the user's profile
     * @param schemes list of active schemes (with rules pre-loaded via JOIN FETCH)
     * @return list of SchemeScore for every scheme
     */
    public List<SchemeScore> evaluate(ProfileRequest profile, List<Scheme> schemes) {
        List<SchemeScore> scores = new ArrayList<>();

        for (Scheme scheme : schemes) {
            SchemeScore score = evaluateScheme(profile, scheme);
            scores.add(score);

            log.debug("Scheme '{}': qualified={}, score={}/{} ({}%)",
                    scheme.getName(), score.isQualified(),
                    score.getTotalScore(), score.getMaxPossibleScore(),
                    String.format("%.1f", score.getScorePercentage()));
        }

        return scores;
    }

    /**
     * Evaluates a single scheme against the profile.
     * Continues evaluating all rules even after a mandatory failure
     * so the response can show ALL unmatched criteria.
     */
    private SchemeScore evaluateScheme(ProfileRequest profile, Scheme scheme) {
        List<RuleEvaluationResult> matchedResults = new ArrayList<>();
        List<RuleEvaluationResult> unmatchedResults = new ArrayList<>();
        boolean qualified = true;
        int totalScore = 0;
        int maxPossibleScore = 0;

        for (SchemeRule rule : scheme.getRules()) {
            RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);
            maxPossibleScore += rule.getWeight();

            if (result.isMatched()) {
                totalScore += result.getScore();
                matchedResults.add(result);
            } else {
                unmatchedResults.add(result);
                if (result.isMandatory()) {
                    qualified = false;
                    log.debug("Scheme '{}' disqualified: mandatory rule failed — {}",
                            scheme.getName(), rule.getDescription());
                }
            }
        }

        return SchemeScore.builder()
                .scheme(scheme)
                .totalScore(totalScore)
                .maxPossibleScore(maxPossibleScore)
                .qualified(qualified)
                .matchedResults(matchedResults)
                .unmatchedResults(unmatchedResults)
                .build();
    }
}
