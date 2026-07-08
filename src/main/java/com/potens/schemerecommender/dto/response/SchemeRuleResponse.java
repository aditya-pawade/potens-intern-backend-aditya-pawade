package com.potens.schemerecommender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemeRuleResponse {

    private Long id;
    private String attribute;
    private String ruleType;
    private String value;
    private String minValue;
    private String maxValue;
    private Integer weight;
    private Boolean isMandatory;
    private String description;
}
