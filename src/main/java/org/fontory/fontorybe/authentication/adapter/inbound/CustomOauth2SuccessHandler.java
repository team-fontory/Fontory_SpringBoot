package org.fontory.fontorybe.authentication.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final MemberOnboardService memberOnboardService;
    private final CookieUtils cookieUtils;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Value("${url.base}") private String baseUrl;
    @Value("${url.path.signup}") private String signUpPath;
    @Value("${url.path.auth}") private String authPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User authUser = (OAuth2User) authentication.getPrincipal();
        Provide provide = authUser.getAttribute("provide");
        Objects.requireNonNull(provide, "OAuth2User must have 'provide' attribute");

        Member member = memberOnboardService.fetchOrCreateMember(provide);
        ResponseCookies cookies = authService.issueAuthCookies(member);
        cookieUtils.addCookies(response, cookies);

        redirectStrategy.sendRedirect(request, response, buildRedirectUrl(member));
    }

    private String buildRedirectUrl(Member member) {
        String path = (member.getStatus() == MemberStatus.ONBOARDING) ? signUpPath : authPath;
        return baseUrl + path;
    }
}
