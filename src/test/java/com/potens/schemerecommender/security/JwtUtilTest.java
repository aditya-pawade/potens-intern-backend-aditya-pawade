package com.potens.schemerecommender.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret",
                "TestSecretKeyThatIsAtLeast32BytesLongForHS256!!");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("Generated token should contain correct username and role")
    void generateToken_containsCorrectClaims() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertEquals("admin", jwtUtil.extractUsername(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    @DisplayName("Valid token should pass validation")
    void validToken_passesValidation() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertTrue(jwtUtil.validateToken(token, "admin"));
    }

    @Test
    @DisplayName("Malformed token should fail validation")
    void malformedToken_failsValidation() {
        assertFalse(jwtUtil.validateToken("not.a.valid.token", "admin"));
    }
}
