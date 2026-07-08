package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.enums.ProfileAttribute;
import com.potens.schemerecommender.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Immutable result of evaluating a single rule against a profile.
 */
@Getter
@AllArgsConstructor
public class RuleEvaluationResult {

    private final boolean matched;
    private final int score;
    private final String explanation;
    private final boolean mandatory;
    private final ProfileAttribute attribute;
    private final RuleType ruleType;
}
