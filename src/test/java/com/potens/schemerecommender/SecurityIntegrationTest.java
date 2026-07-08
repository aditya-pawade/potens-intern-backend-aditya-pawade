package com.potens.schemerecommender;

import com.potens.schemerecommender.controller.AuthController;
import com.potens.schemerecommender.controller.ExplainController;
import com.potens.schemerecommender.controller.RecommendationController;
import com.potens.schemerecommender.controller.SchemeController;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        SchemeController.class,
        RecommendationController.class,
        ExplainController.class,
        AuthController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private SchemeService schemeService;
    @MockBean private RecommendationService recommendationService;
    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private static final String ADMIN_TOKEN = "valid-admin-jwt-token";
    private static final String USER_TOKEN = "valid-user-jwt-token";

    @BeforeEach
    void setUp() {
        when(jwtUtil.extractUsername(ADMIN_TOKEN)).thenReturn("admin");
        when(jwtUtil.validateToken(ADMIN_TOKEN, "admin")).thenReturn(true);
        when(jwtUtil.extractRole(ADMIN_TOKEN)).thenReturn("ADMIN");

        when(jwtUtil.extractUsername(USER_TOKEN)).thenReturn("user");
        when(jwtUtil.validateToken(USER_TOKEN, "user")).thenReturn(true);
        when(jwtUtil.extractRole(USER_TOKEN)).thenReturn("USER");
    }

    @Test
    @DisplayName("Admin endpoint without token should return 401 with UNAUTHORIZED error code")
    void adminEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/schemes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/admin/schemes"));
    }

    @Test
    @DisplayName("Admin endpoint with invalid token should return 401")
    void adminEndpoint_invalidToken_returns401() throws Exception {
        when(jwtUtil.extractUsername("garbage-token"))
                .thenThrow(new RuntimeException("Malformed token"));

        mockMvc.perform(get("/api/admin/schemes")
                        .header("Authorization", "Bearer garbage-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Admin endpoint with expired token should return 401")
    void adminEndpoint_expiredToken_returns401() throws Exception {
        String expiredToken = "expired-jwt-token";
        when(jwtUtil.extractUsername(expiredToken)).thenReturn("admin");
        when(jwtUtil.validateToken(expiredToken, "admin")).thenReturn(false);

        mockMvc.perform(get("/api/admin/schemes")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("USER role accessing admin endpoint should return 403 with ACCESS_DENIED")
    void adminEndpoint_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/schemes")
                        .header("Authorization", "Bearer " + USER_TOKEN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.path").value("/api/admin/schemes"));
    }

    @Test
    @DisplayName("ADMIN role accessing admin endpoint should return 200")
    void adminEndpoint_adminRole_returns200() throws Exception {
        when(schemeService.getAllSchemes(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/admin/schemes")
                        .header("Authorization", "Bearer " + ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/recommend should be accessible without authentication")
    void publicEndpoint_noToken_returns200() throws Exception {
        String profileJson = "{" +
                "\"age\": 25, \"annualIncome\": 300000.0, " +
                "\"gender\": \"MALE\", \"category\": \"GENERAL\", " +
                "\"state\": \"MAHARASHTRA\", \"occupation\": \"FARMER\", " +
                "\"isRural\": true, \"hasDisability\": false, " +
                "\"educationLevel\": \"GRADUATE\", \"familySize\": 4" +
                "}";

        when(recommendationService.recommend(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
