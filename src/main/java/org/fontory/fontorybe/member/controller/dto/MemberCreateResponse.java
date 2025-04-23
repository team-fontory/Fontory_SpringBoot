package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.authentication.application.dto.TokenResponse;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberCreateResponse {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime createdAt;

    public static MemberCreateResponse from(Member member, TokenResponse tokenResponse) {
        return MemberCreateResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
