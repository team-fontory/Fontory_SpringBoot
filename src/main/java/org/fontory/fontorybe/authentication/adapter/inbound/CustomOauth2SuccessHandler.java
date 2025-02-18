package org.fontory.fontorybe.authentication.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User customUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = new HashMap<>();
        Provide provide = customUser.getAttribute("provide");

        if (provide.getMemberId() == null) {
            String token = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(provide.getId()));
            attributes.put("provideId", token);
        } else {
            TokenResponse tokens = authService.login(new UserPrincipal(provide.getMemberId()));
            attributes.put("accessToken", tokens.getAccessToken());
            attributes.put("refreshToken", tokens.getRefreshToken());
        }

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(attributes));
    }
}
