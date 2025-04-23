package org.fontory.fontorybe.authentication.application.port;

import org.fontory.fontorybe.authentication.application.dto.TokenResponse;
import org.fontory.fontorybe.member.domain.Member;

public interface TokenStorage {
    void saveRefreshToken(Member member, String refreshToken);
    void removeRefreshToken(Member member);
    String getRefreshToken(Member member);
    TokenResponse refreshToken(Member member, String refreshToken);
}
