package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.entity.Scheme;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationEngineTest {

    private RecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine(new RuleEvaluator());
    }

    @Test
    @DisplayName("Farmer profile should qualify for PM-KISAN with high score")
    void farmerProfile_qualifiesForPmKisan() {
        Scheme pmKisan = buildPmKisan();
        ProfileRequest profile = buildFarmerProfile();

        List<SchemeScore> scores = engine.evaluate(profile, Collections.singletonList(pmKisan));

        assertEquals(1, scores.size());
        SchemeScore score = scores.get(0);
        assertTrue(score.isQualified());
        assertTrue(score.getTotalScore() > 0);
        assertEquals(pmKisan.getRules().stream().mapToInt(SchemeRule::getWeight).sum(),
                score.getMaxPossibleScore());
    }

    @Test
    @DisplayName("Non-farmer should be disqualified from PM-KISAN (mandatory rule failure)")
    void nonFarmerProfile_disqualifiedFromPmKisan() {
        Scheme pmKisan = buildPmKisan();
        ProfileRequest profile = buildFarmerProfile();
        profile.setOccupation(Occupation.STUDENT);

        List<SchemeScore> scores = engine.evaluate(profile, Collections.singletonList(pmKisan));

        assertEquals(1, scores.size());
        assertFalse(scores.get(0).isQualified());
    }

    @Test
    @DisplayName("Empty scheme list should return empty score list")
    void emptySchemeList_returnsEmptyScores() {
        ProfileRequest profile = buildFarmerProfile();
        List<SchemeScore> scores = engine.evaluate(profile, Collections.emptyList());
        assertTrue(scores.isEmpty());
    }

    @Test
    @DisplayName("Profile matching multiple schemes should produce scores for all")
    void multipleSchemes_allEvaluated() {
        Scheme pmKisan = buildPmKisan();
        Scheme ujjwala = buildUjjwala();
        ProfileRequest profile = buildFarmerProfile();
        profile.setGender(Gender.FEMALE);

        List<SchemeScore> scores = engine.evaluate(profile, Arrays.asList(pmKisan, ujjwala));

        assertEquals(2, scores.size());
        assertNotNull(scores.get(0).getScheme());
        assertNotNull(scores.get(1).getScheme());
    }

    // ──────────────── Helpers ────────────────

    private ProfileRequest buildFarmerProfile() {
        return ProfileRequest.builder()
                .age(30).annualIncome(150000.0).gender(Gender.MALE)
                .category(Category.OBC).state("MAHARASHTRA")
                .occupation(Occupation.FARMER).isRural(true)
                .hasDisability(false).educationLevel(EducationLevel.SECONDARY)
                .familySize(5)
                .build();
    }

    private Scheme buildPmKisan() {
        Scheme scheme = Scheme.builder()
                .id(1L).name("PM-KISAN").category("Agriculture")
                .ministry("Ministry of Agriculture").description("Test")
                .isActive(true).build();
        scheme.addRule(SchemeRule.builder()
                .attribute(ProfileAttribute.OCCUPATION).ruleType(RuleType.ENUM_MATCH)
                .value("FARMER").weight(30).isMandatory(true)
                .description("Must be a farmer").build());
        scheme.addRule(SchemeRule.builder()
                .attribute(ProfileAttribute.ANNUAL_INCOME).ruleType(RuleType.MAX_VALUE)
                .value("180000").weight(25).isMandatory(false)
                .description("Income below 1.8L").build());
        scheme.addRule(SchemeRule.builder()
                .attribute(ProfileAttribute.AGE).ruleType(RuleType.MIN_VALUE)
                .value("18").weight(10).isMandatory(true)
                .description("At least 18 years old").build());
        return scheme;
    }

    private Scheme buildUjjwala() {
        Scheme scheme = Scheme.builder()
                .id(2L).name("PM Ujjwala").category("Energy")
                .ministry("Ministry of Petroleum").description("Test")
                .isActive(true).build();
        scheme.addRule(SchemeRule.builder()
                .attribute(ProfileAttribute.GENDER).ruleType(RuleType.ENUM_MATCH)
                .value("FEMALE").weight(30).isMandatory(true)
                .description("Must be female").build());
        scheme.addRule(SchemeRule.builder()
                .attribute(ProfileAttribute.ANNUAL_INCOME).ruleType(RuleType.MAX_VALUE)
                .value("200000").weight(25).isMandatory(true)
                .description("BPL income").build());
        return scheme;
    }
}
