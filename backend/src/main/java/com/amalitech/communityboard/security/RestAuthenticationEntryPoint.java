package com.amalitech.communityboard.security;

import com.amalitech.communityboard.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("path", request.getRequestURI());
        details.put("code", "AUTHENTICATION_REQUIRED");
        details.put("error", status.getReasonPhrase());

        ResponseDto<Object> body = new ResponseDto<>(status,
                "Full authentication is required to access this resource.",
                details);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
