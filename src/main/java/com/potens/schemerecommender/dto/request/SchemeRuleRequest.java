package com.potens.schemerecommender.dto.request;

import com.potens.schemerecommender.enums.ProfileAttribute;
import com.potens.schemerecommender.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemeRuleRequest {

    @NotNull(message = "Attribute is required")
    private ProfileAttribute attribute;

    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    private String value;

    private String minValue;

    private String maxValue;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1")
    private Integer weight;

    @NotNull(message = "Mandatory flag is required")
    private Boolean isMandatory;

    @NotBlank(message = "Rule description is required")
    private String description;
}
