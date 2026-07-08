package com.potens.schemerecommender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleExplanation {

    private String attribute;
    private String ruleType;
    private String condition;
    private int weight;
    private boolean mandatory;
    private String description;
}
