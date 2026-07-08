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
public class RecommendationResponse {

    private Long schemeId;
    private String schemeName;
    private String schemeCategory;
    private String ministry;
    private String benefitAmount;
    private String benefitDescription;
    private int totalScore;
    private int maxPossibleScore;
    private List<String> matchedCriteria;
    private List<String> unmatchedCriteria;
    private String explanation;
}
