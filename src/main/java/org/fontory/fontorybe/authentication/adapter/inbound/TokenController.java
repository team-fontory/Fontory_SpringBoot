package org.fontory.fontorybe.authentication.adapter.inbound;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenRefreshRequest;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody TokenRefreshRequest tokenRefreshRequest
    ) {
        TokenResponse refreshedTokens = tokenService.refreshToken(tokenRefreshRequest.getRefreshToken());
        return ResponseEntity
                .ok()
                .body(refreshedTokens);
    }

    @PostMapping
    public ResponseEntity<TokenResponse> newToken(
            @RequestParam(required = true) String token
    ) {
        Long provideId = jwtTokenProvider.getProvideId(token);
        Provide provide = provideService.getOrThrownById(provideId);
        TokenResponse newTokens = authService.generateTokens(new UserPrincipal(provide.getMemberId()));
        return ResponseEntity
                .ok()
                .body(newTokens);
    }
}
