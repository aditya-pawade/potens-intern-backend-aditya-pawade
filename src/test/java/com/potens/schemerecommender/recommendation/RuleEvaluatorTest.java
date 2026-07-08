package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.Category;
import com.potens.schemerecommender.enums.EducationLevel;
import com.potens.schemerecommender.enums.Gender;
import com.potens.schemerecommender.enums.Occupation;
import com.potens.schemerecommender.enums.ProfileAttribute;
import com.potens.schemerecommender.enums.RuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator();
    }

    @Test
    @DisplayName("MAX_VALUE: income below threshold should match and award points")
    void maxValue_incomeBelowThreshold_matches() {
        SchemeRule rule = buildRule(ProfileAttribute.ANNUAL_INCOME,
                RuleType.MAX_VALUE, "500000", null, null, 25, false);
        ProfileRequest profile = buildProfile();
        profile.setAnnualIncome(300000.0);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertTrue(result.isMatched());
        assertEquals(25, result.getScore());
        assertFalse(result.isMandatory());
        assertNotNull(result.getExplanation());
    }

    @Test
    @DisplayName("MAX_VALUE: income above threshold should not match")
    void maxValue_incomeAboveThreshold_doesNotMatch() {
        SchemeRule rule = buildRule(ProfileAttribute.ANNUAL_INCOME,
                RuleType.MAX_VALUE, "500000", null, null, 25, false);
        ProfileRequest profile = buildProfile();
        profile.setAnnualIncome(600000.0);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertFalse(result.isMatched());
        assertEquals(0, result.getScore());
    }

    @Test
    @DisplayName("MIN_VALUE: age at exact minimum boundary should match (>=)")
    void minValue_ageAtExactBoundary_matches() {
        SchemeRule rule = buildRule(ProfileAttribute.AGE,
                RuleType.MIN_VALUE, "18", null, null, 10, true);
        ProfileRequest profile = buildProfile();
        profile.setAge(18);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertTrue(result.isMatched());
        assertEquals(10, result.getScore());
        assertTrue(result.isMandatory());
    }

    @Test
    @DisplayName("ENUM_MATCH: matching occupation should match")
    void enumMatch_occupationMatches() {
        SchemeRule rule = buildRule(ProfileAttribute.OCCUPATION,
                RuleType.ENUM_MATCH, "FARMER", null, null, 30, true);
        ProfileRequest profile = buildProfile();
        profile.setOccupation(Occupation.FARMER);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertTrue(result.isMatched());
        assertEquals(30, result.getScore());
    }

    @Test
    @DisplayName("RANGE_CHECK: age within range should match")
    void rangeCheck_ageWithinRange_matches() {
        SchemeRule rule = buildRule(ProfileAttribute.AGE,
                RuleType.RANGE_CHECK, null, "18", "40", 15, true);
        ProfileRequest profile = buildProfile();
        profile.setAge(25);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertTrue(result.isMatched());
        assertEquals(15, result.getScore());
    }

    @Test
    @DisplayName("BOOLEAN_CHECK: rural profile matching rural requirement should match")
    void booleanCheck_ruralMatches() {
        SchemeRule rule = buildRule(ProfileAttribute.IS_RURAL,
                RuleType.BOOLEAN_CHECK, "true", null, null, 20, false);
        ProfileRequest profile = buildProfile();
        profile.setIsRural(true);

        RuleEvaluationResult result = ruleEvaluator.evaluate(rule, profile);

        assertTrue(result.isMatched());
        assertEquals(20, result.getScore());
    }

    // ──────────────── Helpers ────────────────

    private ProfileRequest buildProfile() {
        return ProfileRequest.builder()
                .age(25)
                .annualIncome(300000.0)
                .gender(Gender.MALE)
                .category(Category.GENERAL)
                .state("MAHARASHTRA")
                .occupation(Occupation.SALARIED)
                .isRural(false)
                .hasDisability(false)
                .educationLevel(EducationLevel.GRADUATE)
                .familySize(4)
                .build();
    }

    private SchemeRule buildRule(ProfileAttribute attr, RuleType type,
                                String value, String min, String max,
                                int weight, boolean mandatory) {
        return SchemeRule.builder()
                .attribute(attr)
                .ruleType(type)
                .value(value)
                .minValue(min)
                .maxValue(max)
                .weight(weight)
                .isMandatory(mandatory)
                .description("Test rule for " + attr.name())
                .build();
    }
}
