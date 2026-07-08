package com.potens.schemerecommender.service;

import com.potens.schemerecommender.dto.request.LoginRequest;
import com.potens.schemerecommender.dto.response.LoginResponse;
import com.potens.schemerecommender.entity.AppUser;
import com.potens.schemerecommender.enums.Role;
import com.potens.schemerecommender.repository.UserRepository;
import com.potens.schemerecommender.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("Valid credentials should authenticate and return JWT token with role")
    void validCredentials_returnsTokenWithRole() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        AppUser user = AppUser.builder()
                .username("admin")
                .role(Role.ADMIN)
                .build();

        Authentication successAuth = new UsernamePasswordAuthenticationToken(
                "admin", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successAuth);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("mock-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ADMIN", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("admin");
        verify(jwtUtil).generateToken("admin", "ADMIN");
    }

    @Test
    @DisplayName("Invalid credentials should throw BadCredentialsException")
    void invalidCredentials_throwsBadCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException thrown = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Bad credentials", thrown.getMessage());
        verify(userRepository, never()).findByUsername(any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
