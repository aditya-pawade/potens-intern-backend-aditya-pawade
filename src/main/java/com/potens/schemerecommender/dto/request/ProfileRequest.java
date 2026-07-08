package com.potens.schemerecommender.dto.request;

import com.potens.schemerecommender.enums.Category;
import com.potens.schemerecommender.enums.EducationLevel;
import com.potens.schemerecommender.enums.Gender;
import com.potens.schemerecommender.enums.Occupation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be non-negative")
    @Max(value = 150, message = "Age must be realistic")
    private Integer age;

    @NotNull(message = "Annual income is required")
    @Min(value = 0, message = "Annual income must be non-negative")
    private Double annualIncome;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Occupation is required")
    private Occupation occupation;

    @NotNull(message = "Rural status is required")
    private Boolean isRural;

    @NotNull(message = "Disability status is required")
    private Boolean hasDisability;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;

    @NotNull(message = "Family size is required")
    @Positive(message = "Family size must be positive")
    private Integer familySize;
}
