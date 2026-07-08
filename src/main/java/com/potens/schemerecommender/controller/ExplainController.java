package com.potens.schemerecommender.controller;

import com.potens.schemerecommender.dto.response.ApiResponse;
import com.potens.schemerecommender.dto.response.ExplainResponse;
import com.potens.schemerecommender.service.SchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/explain")
@Tag(name = "Scheme Explanation", description = "Eligibility rules and logic explanation")
public class ExplainController {

    private static final Logger log = LoggerFactory.getLogger(ExplainController.class);

    private final SchemeService schemeService;

    public ExplainController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @GetMapping("/{schemeId}")
    @Operation(summary = "Explain scheme eligibility",
               description = "Returns eligibility rules, required attributes, and business logic for a scheme")
    public ResponseEntity<ApiResponse<ExplainResponse>> explainScheme(@PathVariable Long schemeId) {

        log.info("Explaining scheme with id: {}", schemeId);
        ExplainResponse explanation = schemeService.explainScheme(schemeId);
        return ResponseEntity.ok(ApiResponse.success("Scheme eligibility explained", explanation));
    }
}
