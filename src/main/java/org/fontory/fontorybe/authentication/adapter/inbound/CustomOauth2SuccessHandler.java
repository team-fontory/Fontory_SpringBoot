package org.fontory.fontorybe.authentication.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("OAuth2 authentication success handler triggered");
        
        OAuth2User authUser = (OAuth2User) authentication.getPrincipal();
        Provide provide = authUser.getAttribute("provide");
        Objects.requireNonNull(provide, "OAuth2User must have 'provide' attribute");
        
        log.info("Processing successful OAuth2 login: provideId={}, provider={}, email={}", 
                provide.getId(), provide.getProvider(), provide.getEmail());

        Member member = memberOnboardService.fetchOrCreateMember(provide);
        log.info("Member fetched/created: memberId={}, status={}",
                member.getId(), member.getStatus());
        
        ResponseCookies cookies = authService.issueAuthCookies(member);
        log.debug("Auth cookies issued for member: memberId={}", member.getId());
        
        cookieUtils.addCookies(response, cookies);
        log.debug("Auth cookies added to response: memberId={}", member.getId());

        String redirectUrl = buildRedirectUrl(member);
        log.info("Redirecting user after successful OAuth2 login: memberId={}, status={}, redirectUrl={}", 
                member.getId(), member.getStatus(), redirectUrl);
        
        redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

    private String buildRedirectUrl(Member member) {
        String path = (member.getStatus() == MemberStatus.ONBOARDING) ? signUpPath : authPath;
        String redirectUrl = baseUrl + path;
        
        log.debug("Building redirect URL: memberStatus={}, path={}, fullUrl={}", 
                member.getStatus(), path, redirectUrl);
        
        return redirectUrl;
    }
}
