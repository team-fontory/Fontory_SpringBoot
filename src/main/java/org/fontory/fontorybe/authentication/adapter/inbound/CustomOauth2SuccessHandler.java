package org.fontory.fontorybe.authentication.adapter.inbound;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${url.base}") private String BaseURL;
    @Value("${url.path.signup}") private String SignUpPath;
    @Value("${url.path.auth}") private String AuthPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String SIGNUP_URL = BaseURL + SignUpPath;
        String AUTH_URL = BaseURL + AuthPath;

        OAuth2User authUser = (OAuth2User) authentication.getPrincipal();
        Provide provide = authUser.getAttribute("provide");

        String token = jwtTokenProvider.generateTemporalProvideToken(String.valueOf(provide.getId()));

        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        System.out.println("token = " + token);
        System.out.println("provide = " + provide);
        System.out.println("SIGNUP_URL = " + SIGNUP_URL);
        System.out.println("AUTH_URL = " + AUTH_URL);
        System.out.println("request = " + request);
        System.out.println("response = " + response);

        if (provide.getMemberId() == null) {
            redirectStrategy.sendRedirect(request, response, SIGNUP_URL + "?token=" + token);
        } else {
            redirectStrategy.sendRedirect(request, response, AUTH_URL + "?token=" + token);
        }
    }
}
