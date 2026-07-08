package com.potens.schemerecommender;

import com.potens.schemerecommender.controller.RecommendationController;
import com.potens.schemerecommender.controller.SchemeController;
import com.potens.schemerecommender.exception.DuplicateResourceException;
import com.potens.schemerecommender.exception.InvalidRuleException;
import com.potens.schemerecommender.exception.ResourceNotFoundException;
import com.potens.schemerecommender.security.CustomAccessDeniedHandler;
import com.potens.schemerecommender.security.CustomUserDetailsService;
import com.potens.schemerecommender.security.JwtAuthenticationEntryPoint;
import com.potens.schemerecommender.security.JwtAuthenticationFilter;
import com.potens.schemerecommender.security.JwtUtil;
import com.potens.schemerecommender.security.SecurityConfig;
import com.potens.schemerecommender.service.AuthService;
import com.potens.schemerecommender.service.RecommendationService;
import com.potens.schemerecommender.service.SchemeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RecommendationController.class, SchemeController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class ValidationAndErrorHandlerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RecommendationService recommendationService;
    @MockBean private SchemeService schemeService;
    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String ADMIN_TOKEN = "admin-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.extractUsername(ADMIN_TOKEN)).thenReturn("admin");
        when(jwtUtil.validateToken(ADMIN_TOKEN, "admin")).thenReturn(true);
        when(jwtUtil.extractRole(ADMIN_TOKEN)).thenReturn("ADMIN");
    }

    @Test
    @DisplayName("Missing required ProfileRequest fields should return 400 VALIDATION_ERROR")
    void missingProfileFields_returns400WithFieldErrors() throws Exception {
        String incompleteProfile = "{" +
                "\"annualIncome\": 300000.0, " +
                "\"category\": \"GENERAL\", " +
                "\"state\": \"MAHARASHTRA\", " +
                "\"isRural\": true, \"hasDisability\": false, " +
                "\"educationLevel\": \"GRADUATE\", \"familySize\": 4" +
                "}";

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteProfile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("Invalid enum value in JSON should return 400 MALFORMED_REQUEST")
    void invalidEnumValue_returns400() throws Exception {
        String badEnumProfile = "{" +
                "\"age\": 25, \"annualIncome\": 300000.0, " +
                "\"gender\": \"INVALID_GENDER\", \"category\": \"GENERAL\", " +
                "\"state\": \"MAHARASHTRA\", \"occupation\": \"FARMER\", " +
                "\"isRural\": true, \"hasDisability\": false, " +
                "\"educationLevel\": \"GRADUATE\", \"familySize\": 4" +
                "}";

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badEnumProfile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST"));
    }

    @Test
    @DisplayName("Non-existent scheme ID should return 404 RESOURCE_NOT_FOUND")
    void schemeNotFound_returns404() throws Exception {
        when(schemeService.getSchemeById(999L))
                .thenThrow(new ResourceNotFoundException("Scheme", "id", 999L));

        mockMvc.perform(get("/api/admin/schemes/999")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Scheme not found with id: '999'"));
    }

    @Test
    @DisplayName("Duplicate scheme name should return 409 DUPLICATE_RESOURCE")
    void duplicateSchemeName_returns409() throws Exception {
        when(schemeService.createScheme(any()))
                .thenThrow(new DuplicateResourceException("Scheme", "name", "PM-KISAN"));

        String schemeJson = "{" +
                "\"name\": \"PM-KISAN\", \"description\": \"Test\", " +
                "\"category\": \"Agriculture\", \"ministry\": \"Test Ministry\", " +
                "\"rules\": [{" +
                    "\"attribute\": \"AGE\", \"ruleType\": \"MIN_VALUE\", " +
                    "\"value\": \"18\", \"weight\": 10, " +
                    "\"isMandatory\": true, \"description\": \"Must be 18+\"" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/admin/schemes")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(schemeJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("Invalid rule configuration should return 400 INVALID_RULE")
    void invalidRuleConfig_returns400() throws Exception {
        when(schemeService.createScheme(any()))
                .thenThrow(new InvalidRuleException(
                        com.potens.schemerecommender.enums.RuleType.RANGE_CHECK,
                        "minValue is required"));

        String schemeJson = "{" +
                "\"name\": \"Test Scheme\", \"description\": \"Test\", " +
                "\"category\": \"Test\", \"ministry\": \"Test\", " +
                "\"rules\": [{" +
                    "\"attribute\": \"AGE\", \"ruleType\": \"RANGE_CHECK\", " +
                    "\"weight\": 10, " +
                    "\"isMandatory\": false, \"description\": \"Age range\"" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/admin/schemes")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(schemeJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_RULE"));
    }
}
