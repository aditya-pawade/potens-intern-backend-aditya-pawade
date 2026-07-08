package com.potens.schemerecommender.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemeResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String ministry;
    private String benefitAmount;
    private String benefitDescription;
    private String eligibilitySummary;
    private Boolean isActive;
    private List<SchemeRuleResponse> rules;
    private String createdAt;
    private String updatedAt;
}
