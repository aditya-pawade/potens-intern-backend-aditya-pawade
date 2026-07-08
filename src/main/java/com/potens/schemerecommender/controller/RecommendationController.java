package com.potens.schemerecommender.controller;

import com.potens.schemerecommender.dto.request.ProfileRequest;
import com.potens.schemerecommender.dto.response.ApiResponse;
import com.potens.schemerecommender.dto.response.RecommendationResponse;
import com.potens.schemerecommender.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Recommendations", description = "Profile-to-scheme recommendation engine")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommend")
    @Operation(summary = "Get scheme recommendations",
               description = "Submit a profile and receive top 3 matching government schemes with explanations")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> recommend(
            @Valid @RequestBody ProfileRequest profile) {

        log.info("Received recommendation request for occupation: {}, category: {}",
                profile.getOccupation(), profile.getCategory());

        List<RecommendationResponse> recommendations = recommendationService.recommend(profile);

        String message = recommendations.isEmpty()
                ? "No matching schemes found for the provided profile"
                : String.format("Found %d matching scheme(s)", recommendations.size());

        return ResponseEntity.ok(ApiResponse.success(message, recommendations));
    }
}
