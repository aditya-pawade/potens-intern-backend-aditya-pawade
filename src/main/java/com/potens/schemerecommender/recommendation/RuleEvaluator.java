package com.potens.schemerecommender.recommendation;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.ProfileAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Evaluates a single SchemeRule against a ProfileRequest.
 * Pure function: input → output, no state, no side effects.
 *
 * <p>Trade-off note: All profile values are extracted as Strings for a uniform
 * evaluation interface. A production system could use typed value objects to
 * avoid stringify→parse cycles for numeric fields. For this assignment scope,
 * the String approach keeps the code simple and interview-explainable.</p>
 */
@Component
public class RuleEvaluator {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluator.class);

    /**
     * Evaluates a single rule against a profile.
     *
     * @param rule    the eligibility rule to evaluate
     * @param profile the user's submitted profile
     * @return result containing match status, score, and explanation
     */
    public RuleEvaluationResult evaluate(SchemeRule rule, ProfileRequest profile) {
        String profileValue = extractProfileValue(profile, rule.getAttribute());
        boolean matched;
        String explanation;

        switch (rule.getRuleType()) {

            case ENUM_MATCH:
                matched = profileValue.trim().equalsIgnoreCase(
                        rule.getValue() != null ? rule.getValue().trim() : "");
                explanation = matched
                        ? String.format("Your %s (%s) matches the requirement",
                            formatAttribute(rule.getAttribute()), formatValue(profileValue))
                        : String.format("Your %s (%s) does not match the required value (%s)",
                            formatAttribute(rule.getAttribute()), formatValue(profileValue),
                            formatValue(rule.getValue()));
                break;

            case RANGE_CHECK:
                double rangeVal = parseDouble(rule, profileValue);
                double minBound = parseDouble(rule, rule.getMinValue());
                double maxBound = parseDouble(rule, rule.getMaxValue());
                matched = rangeVal >= minBound && rangeVal <= maxBound;
                explanation = matched
                        ? String.format("Your %s (%.0f) is within the required range (%.0f to %.0f)",
                            formatAttribute(rule.getAttribute()), rangeVal, minBound, maxBound)
                        : String.format("Your %s (%.0f) is outside the required range (%.0f to %.0f)",
                            formatAttribute(rule.getAttribute()), rangeVal, minBound, maxBound);
                break;

            case BOOLEAN_CHECK:
                boolean profileBool = Boolean.parseBoolean(profileValue);
                boolean requiredBool = Boolean.parseBoolean(rule.getValue());
                matched = profileBool == requiredBool;
                explanation = matched
                        ? String.format("You meet the requirement: %s", rule.getDescription())
                        : String.format("Does not meet: %s", rule.getDescription());
                break;

            case MIN_VALUE:
                double minVal = parseDouble(rule, profileValue);
                double minRequired = parseDouble(rule, rule.getValue());
                matched = minVal >= minRequired;
                explanation = matched
                        ? String.format("Your %s (%.0f) meets the minimum requirement of %.0f",
                            formatAttribute(rule.getAttribute()), minVal, minRequired)
                        : String.format("Your %s (%.0f) is below the minimum requirement of %.0f",
                            formatAttribute(rule.getAttribute()), minVal, minRequired);
                break;

            case MAX_VALUE:
                double maxVal = parseDouble(rule, profileValue);
                double maxAllowed = parseDouble(rule, rule.getValue());
                matched = maxVal <= maxAllowed;
                explanation = matched
                        ? String.format("Your %s (%.0f) is within the maximum limit of %.0f",
                            formatAttribute(rule.getAttribute()), maxVal, maxAllowed)
                        : String.format("Your %s (%.0f) exceeds the maximum limit of %.0f",
                            formatAttribute(rule.getAttribute()), maxVal, maxAllowed);
                break;

            default:
                throw new IllegalStateException("Unhandled RuleType: " + rule.getRuleType());
        }

        int score = matched ? rule.getWeight() : 0;
        return new RuleEvaluationResult(matched, score, explanation,
                rule.getIsMandatory(), rule.getAttribute(), rule.getRuleType());
    }

    // ──────────────── Profile Value Extraction ────────────────

    private String extractProfileValue(ProfileRequest profile, ProfileAttribute attribute) {
        switch (attribute) {
            case AGE:              return String.valueOf(profile.getAge());
            case ANNUAL_INCOME:    return String.valueOf(profile.getAnnualIncome());
            case GENDER:           return profile.getGender().name();
            case CATEGORY:         return profile.getCategory().name();
            case STATE:            return profile.getState().trim();
            case OCCUPATION:       return profile.getOccupation().name();
            case IS_RURAL:         return String.valueOf(profile.getIsRural());
            case HAS_DISABILITY:   return String.valueOf(profile.getHasDisability());
            case EDUCATION_LEVEL:  return profile.getEducationLevel().name();
            case FAMILY_SIZE:      return String.valueOf(profile.getFamilySize());
            default:
                throw new IllegalStateException("Unhandled ProfileAttribute: " + attribute);
        }
    }

    // ──────────────── Formatting Helpers ────────────────

    private String formatAttribute(ProfileAttribute attribute) {
        return attribute.name().toLowerCase().replace('_', ' ');
    }

    private String formatValue(String value) {
        if (value == null) return "N/A";
        String[] parts = value.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(' ');
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    private double parseDouble(SchemeRule rule, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("Misconfigured rule data: cannot parse '{}' as number for rule {} on attribute {}",
                    value, rule.getRuleType(), rule.getAttribute());
            throw new IllegalStateException(
                    String.format("Misconfigured rule data: '%s' is not a valid number for %s rule on %s",
                            value, rule.getRuleType(), rule.getAttribute()));
        }
    }
}
