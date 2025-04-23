package org.fontory.fontorybe.authentication.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseCookie;

@Builder
@Setter @Getter
public class ResponseCookies {
    private final ResponseCookie accessTokenCookie;
    private final ResponseCookie refreshTokenCookie;

    public static ResponseCookies from(ResponseCookie accessTokenCookie, ResponseCookie refreshTokenCookie) {
        return ResponseCookies.builder()
                .accessTokenCookie(accessTokenCookie)
                .refreshTokenCookie(refreshTokenCookie)
                .build();
    }

    public String accessTokenCookieToString() {
        return accessTokenCookie.toString();
    }

    public String refreshTokenCookieToString() {
        return refreshTokenCookie.toString();
    }
}
