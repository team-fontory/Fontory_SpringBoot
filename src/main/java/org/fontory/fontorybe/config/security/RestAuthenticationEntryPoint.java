package org.fontory.fontorybe.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req,
                         HttpServletResponse res,
                         AuthenticationException authEx) throws IOException {
        cookieUtils.clearAuthCookies(res);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");

        String message;
        if (authEx instanceof InsufficientAuthenticationException) {
            message = "Authentication Required.";
        } else {
            message = authEx.getMessage();
        }

        // 4) JSON 바디 작성
        BaseErrorResponse body = new BaseErrorResponse(message);
        String json = objectMapper.writeValueAsString(body);
        res.getWriter().write(json);
        res.getWriter().flush();
    }
}