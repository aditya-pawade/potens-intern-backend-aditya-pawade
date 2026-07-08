package com.potens.schemerecommender.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemeRequest {

    @NotBlank(message = "Scheme name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotBlank(message = "Ministry is required")
    @Size(max = 200, message = "Ministry must not exceed 200 characters")
    private String ministry;

    private String benefitAmount;

    private String benefitDescription;

    private String eligibilitySummary;

    @NotEmpty(message = "At least one rule is required")
    @Valid
    private List<SchemeRuleRequest> rules;
}
