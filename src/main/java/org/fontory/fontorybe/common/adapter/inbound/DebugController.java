package org.fontory.fontorybe.common.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.fontory.fontorybe.authentication.application.AuthConstants.*;

@RestController
@RequiredArgsConstructor
public class DebugController {
    private final FontRequestProducer fontRequestProducer;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtils cookieUtils;

    @Value("${commit.hash}")
    public String commitHash;

    @GetMapping("/health-check")
    public String healthCheck() { return commitHash; }

    @GetMapping("/debug/sqs-test")
    public String sqsTest() {
        FontRequestProduceDto dto = FontRequestProduceDto.builder()
                .memberId(1L)
                .fontId(1L)
                .fontName("testFontName")
                .templateURL("https://fontory-profile-image.s3.ap-northeast-2.amazonaws.com/a88cd9a0-36fb-4a4f-abd8-696e6235a0a9.jpeg")
                .author("testMemberNickname")
                .requestUUID(MDC.get("requestId"))
                .build();
        fontRequestProducer.sendFontRequest(dto);
        return "test";
    }

    @GetMapping("/debug/token-cookies")
    public String cookies(HttpServletRequest request) {
        Optional<String> accessCookie = cookieUtils.extractTokenFromCookieInRequest(request, ACCESS_TOKEN_COOKIE_NAME);
        Optional<String> refreshCookie = cookieUtils.extractTokenFromCookieInRequest(request, REFRESH_TOKEN_COOKIE_NAME);

        String accessToken = accessCookie.orElse(null);
        String refreshToken = refreshCookie.orElse(null);

        return "accessToken: " + accessToken + "\nrefreshToken: " + refreshToken;
    }

    @GetMapping("/debug/auth/me")
    public String me(
            HttpServletRequest request,
            @Login UserPrincipal userPrincipal
    ) {
//        if userprincipal null, exception in argument Resolver
        return String.valueOf(userPrincipal.getId());
    }

    @GetMapping("/debug/logout")
    public void me(HttpServletResponse res) {
        cookieUtils.clearAuthCookies(res);
    }
}
