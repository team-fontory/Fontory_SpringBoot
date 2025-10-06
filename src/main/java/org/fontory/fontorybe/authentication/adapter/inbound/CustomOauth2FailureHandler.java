package org.fontory.fontorybe.authentication.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOauth2FailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        log.warn("OAuth2 authentication failed: errorMessage={}, exceptionType={}", 
                exception.getMessage(), exception.getClass().getSimpleName());
        
        String requestUrl = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        log.debug("Failed OAuth2 request details: url={}, queryString={}", requestUrl, queryString);
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("message", "error occurred during authentication");
        
        log.info("Sending OAuth2 authentication failure response: status={}", HttpServletResponse.SC_UNAUTHORIZED);
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(attributes));
    }
}
