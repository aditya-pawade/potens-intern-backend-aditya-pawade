package com.potens.schemerecommender.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potens.schemerecommender.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns JSON 401 response for unauthenticated requests on protected endpoints.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Authentication required. Please provide a valid JWT token.")
                .errorCode("UNAUTHORIZED")
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
