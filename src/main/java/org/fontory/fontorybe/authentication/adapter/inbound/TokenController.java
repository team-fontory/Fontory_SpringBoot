package org.fontory.fontorybe.authentication.adapter.inbound;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;
    private final ProvideService provideService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    
    /**
     * Safely mask token for logging (first 6 chars + "..." + last 4 chars)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "[PROTECTED]";
        }
        int visiblePrefix = 6;
        int visibleSuffix = 4;
        return token.substring(0, visiblePrefix) + "..." + 
               token.substring(token.length() - visibleSuffix);
    }
    
    /**
     * Convert an object to JSON string for logging (with token masking)
     */
    private String safeLogTokenResponse(TokenResponse response) {
        try {
            return "TokenResponse{" +
                   "accessToken='" + maskToken(response.getAccessToken()) + "', " +
                   "refreshToken='" + maskToken(response.getRefreshToken()) + "'" +
                   "}";
        } catch (Exception e) {
            log.warn("Failed to format token response for logging: {}", e.getMessage());
            return "[TokenResponse]";
        }
    }

    @Operation(
            summary = "토큰 재발급"
    )
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody TokenRefreshRequest tokenRefreshRequest
    ) {
        log.info("Request received: Refresh token request");
        
        String refreshToken = tokenRefreshRequest.getRefreshToken();
        log.debug("Processing refresh token: {}", maskToken(refreshToken));
        
        Long memberId = jwtTokenProvider.getMemberId(tokenRefreshRequest.getRefreshToken());
        Member requestMember = memberService.getOrThrowById(memberId);
        
        log.debug("Processing refresh token for member ID: {}, nickname: {}", 
                memberId, requestMember.getNickname());
        
        TokenResponse refreshedTokens = tokenService.refreshToken(requestMember, refreshToken);
        log.info("Response sent: Token refreshed successfully for member ID: {}, response: {}", 
                memberId, safeLogTokenResponse(refreshedTokens));

        return ResponseEntity
                .ok()
                .body(refreshedTokens);
    }

    @Operation(
            summary = "토큰 발급"
    )
    @PostMapping
    public ResponseEntity<TokenResponse> newToken(
            @RequestParam(required = true) String provideToken
    ) {
        log.info("Request received: Generate new token from provide token: {}", maskToken(provideToken));
        
        Long provideId = jwtTokenProvider.getProvideId(provideToken);
        Provide provide = provideService.getOrThrownById(provideId);
        Long memberId = provide.getMemberId();
        
        log.debug("Processing token generation for member ID: {} with provider: {}", 
                memberId, provide.getProvider());
        
        Member requestMember = memberService.getOrThrowById(memberId);
        log.debug("Found member with nickname: {}", requestMember.getNickname());
        
        TokenResponse newTokens = authService.generateTokens(requestMember);
        log.info("Response sent: New token generated successfully for member ID: {}, response: {}", 
                memberId, safeLogTokenResponse(newTokens));

        return ResponseEntity
                .ok()
                .body(newTokens);
    }
}
