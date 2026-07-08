package com.potens.schemerecommender.mapper;

import com.potens.schemerecommender.dto.request.SchemeRequest;
import com.potens.schemerecommender.dto.request.SchemeRuleRequest;
import com.potens.schemerecommender.dto.response.ExplainResponse;
import com.potens.schemerecommender.dto.response.RuleExplanation;
import com.potens.schemerecommender.dto.response.SchemeResponse;
import com.potens.schemerecommender.dto.response.SchemeRuleResponse;
import com.potens.schemerecommender.entity.Scheme;
import com.potens.schemerecommender.entity.SchemeRule;
import com.potens.schemerecommender.enums.RuleType;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Manual static mapper for Scheme / SchemeRule conversions.
 * No MapStruct — explicit, debuggable, no magic.
 */
public final class SchemeMapper {

    private SchemeMapper() {
        // Utility class
    }

    // ──────────────── Entity → Response ────────────────

    public static SchemeResponse toResponse(Scheme scheme) {
        return SchemeResponse.builder()
                .id(scheme.getId())
                .name(scheme.getName())
                .description(scheme.getDescription())
                .category(scheme.getCategory())
                .ministry(scheme.getMinistry())
                .benefitAmount(scheme.getBenefitAmount())
                .benefitDescription(scheme.getBenefitDescription())
                .eligibilitySummary(scheme.getEligibilitySummary())
                .isActive(scheme.getIsActive())
                .rules(scheme.getRules().stream()
                        .map(SchemeMapper::toRuleResponse)
                        .collect(Collectors.toList()))
                .createdAt(scheme.getCreatedAt() != null ? scheme.getCreatedAt().toString() : null)
                .updatedAt(scheme.getUpdatedAt() != null ? scheme.getUpdatedAt().toString() : null)
                .build();
    }

    /**
     * Light response without triggering lazy-loaded rules.
     * Used for paginated listing.
     */
    public static SchemeResponse toResponseWithoutRules(Scheme scheme) {
        return SchemeResponse.builder()
                .id(scheme.getId())
                .name(scheme.getName())
                .description(scheme.getDescription())
                .category(scheme.getCategory())
                .ministry(scheme.getMinistry())
                .benefitAmount(scheme.getBenefitAmount())
                .benefitDescription(scheme.getBenefitDescription())
                .eligibilitySummary(scheme.getEligibilitySummary())
                .isActive(scheme.getIsActive())
                .rules(Collections.emptyList())
                .createdAt(scheme.getCreatedAt() != null ? scheme.getCreatedAt().toString() : null)
                .updatedAt(scheme.getUpdatedAt() != null ? scheme.getUpdatedAt().toString() : null)
                .build();
    }

    public static SchemeRuleResponse toRuleResponse(SchemeRule rule) {
        return SchemeRuleResponse.builder()
                .id(rule.getId())
                .attribute(rule.getAttribute().name())
                .ruleType(rule.getRuleType().name())
                .value(rule.getValue())
                .minValue(rule.getMinValue())
                .maxValue(rule.getMaxValue())
                .weight(rule.getWeight())
                .isMandatory(rule.getIsMandatory())
                .description(rule.getDescription())
                .build();
    }

    // ──────────────── Entity → ExplainResponse ────────────────

    public static ExplainResponse toExplainResponse(Scheme scheme) {
        int totalWeight = scheme.getRules().stream()
                .mapToInt(SchemeRule::getWeight)
                .sum();

        return ExplainResponse.builder()
                .schemeId(scheme.getId())
                .schemeName(scheme.getName())
                .description(scheme.getDescription())
                .eligibilitySummary(scheme.getEligibilitySummary())
                .benefitAmount(scheme.getBenefitAmount())
                .benefitDescription(scheme.getBenefitDescription())
                .totalRuleWeight(totalWeight)
                .rules(scheme.getRules().stream()
                        .map(SchemeMapper::toRuleExplanation)
                        .collect(Collectors.toList()))
                .build();
    }

    private static RuleExplanation toRuleExplanation(SchemeRule rule) {
        return RuleExplanation.builder()
                .attribute(rule.getAttribute().name())
                .ruleType(rule.getRuleType().name())
                .condition(buildConditionText(rule))
                .weight(rule.getWeight())
                .mandatory(rule.getIsMandatory())
                .description(rule.getDescription())
                .build();
    }

    private static String buildConditionText(SchemeRule rule) {
        switch (rule.getRuleType()) {
            case ENUM_MATCH:
                return rule.getAttribute().name() + " must be " + rule.getValue();
            case RANGE_CHECK:
                return rule.getAttribute().name() + " must be between "
                        + rule.getMinValue() + " and " + rule.getMaxValue();
            case BOOLEAN_CHECK:
                return rule.getAttribute().name() + " must be " + rule.getValue();
            case MIN_VALUE:
                return rule.getAttribute().name() + " must be at least " + rule.getValue();
            case MAX_VALUE:
                return rule.getAttribute().name() + " must be at most " + rule.getValue();
            default:
                throw new IllegalStateException("Unhandled RuleType: " + rule.getRuleType());
        }
    }

    // ──────────────── Request → Entity ────────────────

    public static Scheme toEntity(SchemeRequest request) {
        Scheme scheme = Scheme.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .ministry(request.getMinistry())
                .benefitAmount(request.getBenefitAmount())
                .benefitDescription(request.getBenefitDescription())
                .eligibilitySummary(request.getEligibilitySummary())
                .build();

        if (request.getRules() != null) {
            for (SchemeRuleRequest ruleReq : request.getRules()) {
                scheme.addRule(toRuleEntity(ruleReq));
            }
        }

        return scheme;
    }

    public static SchemeRule toRuleEntity(SchemeRuleRequest request) {
        return SchemeRule.builder()
                .attribute(request.getAttribute())
                .ruleType(request.getRuleType())
                .value(request.getValue())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .weight(request.getWeight())
                .isMandatory(request.getIsMandatory())
                .description(request.getDescription())
                .build();
    }
}
