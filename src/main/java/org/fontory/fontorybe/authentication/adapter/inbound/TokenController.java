package org.fontory.fontorybe.authentication.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenRefreshRequest;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final ProvideService provideService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody TokenRefreshRequest tokenRefreshRequest
    ) {
        String refreshToken = tokenRefreshRequest.getRefreshToken();
        Long memberId = jwtTokenProvider.getMemberId(tokenRefreshRequest.getRefreshToken());
        Member requestMember = memberService.getOrThrowById(memberId);
        TokenResponse refreshedTokens = tokenService.refreshToken(requestMember, refreshToken);

        return ResponseEntity
                .ok()
                .body(refreshedTokens);
    }

    @PostMapping
    public ResponseEntity<TokenResponse> newToken(
            @RequestParam(required = true) String provideToken
    ) {
        Long provideId = jwtTokenProvider.getProvideId(provideToken);
        Provide provide = provideService.getOrThrownById(provideId);
        Member requestMember = memberService.getOrThrowById(provide.getMemberId());
        TokenResponse newTokens = authService.generateTokens(requestMember);

        return ResponseEntity
                .ok()
                .body(newTokens);
    }
}
