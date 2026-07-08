package com.potens.schemerecommender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplainResponse {

    private Long schemeId;
    private String schemeName;
    private String description;
    private String eligibilitySummary;
    private String benefitAmount;
    private String benefitDescription;
    private int totalRuleWeight;
    private List<RuleExplanation> rules;
}
