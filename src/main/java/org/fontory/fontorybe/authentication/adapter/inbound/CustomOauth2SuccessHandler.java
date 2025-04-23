package org.fontory.fontorybe.authentication.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
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
    private final MemberService memberService;
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

        Member member = fetchOrCreateMember(provide);
        ResponseCookies cookies = authService.issueAuthCookies(member);
        cookieUtils.addCookies(response, cookies);

        redirectStrategy.sendRedirect(request, response, buildRedirectUrl(provide));
    }

    private Member fetchOrCreateMember(Provide provide) {
        if (provide.getMemberId() == null) {
            MemberCreateRequest req = MemberCreateRequest.defaultMemberCreateRequest();
            return memberService.create(req, provide);
        } else {
            return memberService.getOrThrowById(provide.getMemberId());
        }
    }

    private String buildRedirectUrl(Provide provide) {
        String path = (provide.getMemberId() == null) ? signUpPath : authPath;
        return baseUrl + path;
    }
}
