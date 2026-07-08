package com.potens.schemerecommender.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.potens.schemerecommender.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns JSON 403 response when an authenticated user lacks the required role.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("You do not have permission to access this resource")
                .errorCode("ACCESS_DENIED")
                .timestamp(LocalDateTime.now().toString())
                .path(request.getRequestURI())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
