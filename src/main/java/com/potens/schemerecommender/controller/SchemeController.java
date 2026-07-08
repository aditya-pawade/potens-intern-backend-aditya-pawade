package com.potens.schemerecommender.controller;

import com.potens.schemerecommender.dto.request.SchemeRequest;
import com.potens.schemerecommender.dto.response.ApiResponse;
import com.potens.schemerecommender.dto.response.SchemeResponse;
import com.potens.schemerecommender.service.SchemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/schemes")
@Tag(name = "Scheme Management", description = "Admin CRUD operations for government schemes")
@SecurityRequirement(name = "bearerAuth")
public class SchemeController {

    private static final Logger log = LoggerFactory.getLogger(SchemeController.class);

    private final SchemeService schemeService;

    public SchemeController(SchemeService schemeService) {
        this.schemeService = schemeService;
    }

    @GetMapping
    @Operation(summary = "List all schemes", description = "Paginated list of all schemes (without rules)")
    public ResponseEntity<ApiResponse<Page<SchemeResponse>>> getAllSchemes(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        log.info("Fetching schemes page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SchemeResponse> schemes = schemeService.getAllSchemes(pageable);
        return ResponseEntity.ok(ApiResponse.success("Schemes retrieved successfully", schemes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scheme by ID", description = "Returns scheme with its eligibility rules")
    public ResponseEntity<ApiResponse<SchemeResponse>> getSchemeById(@PathVariable Long id) {

        log.info("Fetching scheme with id: {}", id);
        SchemeResponse scheme = schemeService.getSchemeById(id);
        return ResponseEntity.ok(ApiResponse.success("Scheme retrieved successfully", scheme));
    }

    @PostMapping
    @Operation(summary = "Create a new scheme", description = "Creates a scheme with eligibility rules")
    public ResponseEntity<ApiResponse<SchemeResponse>> createScheme(
            @Valid @RequestBody SchemeRequest request) {

        log.info("Creating scheme: {}", request.getName());
        SchemeResponse created = schemeService.createScheme(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Scheme created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a scheme", description = "Replaces scheme data and rules")
    public ResponseEntity<ApiResponse<SchemeResponse>> updateScheme(
            @PathVariable Long id,
            @Valid @RequestBody SchemeRequest request) {

        log.info("Updating scheme with id: {}", id);
        SchemeResponse updated = schemeService.updateScheme(id, request);
        return ResponseEntity.ok(ApiResponse.success("Scheme updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scheme", description = "Permanently deletes a scheme and its rules")
    public ResponseEntity<ApiResponse<Void>> deleteScheme(@PathVariable Long id) {

        log.info("Deleting scheme with id: {}", id);
        schemeService.deleteScheme(id);
        return ResponseEntity.ok(ApiResponse.success("Scheme deleted successfully", null));
    }
}
