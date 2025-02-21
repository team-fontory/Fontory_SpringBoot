package org.fontory.fontorybe.authentication.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenRefreshRequest;
import org.fontory.fontorybe.authentication.adapter.inbound.dto.TokenResponse;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody TokenRefreshRequest tokenRefreshRequest
    ) {
        TokenResponse newTokens = tokenService.refreshToken(tokenRefreshRequest.getRefreshToken());
        return ResponseEntity
                .ok()
                .body(newTokens);
    }

}
