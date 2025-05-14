package org.fontory.fontorybe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "사용자 - 인증관련", description = "인증/토큰")
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "로그아웃"
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response,
            @Login UserPrincipal userPrincipal) {
        authService.clearAuthCookies(response, userPrincipal.getId());

        return ResponseEntity.noContent().build();
    }
}
