package org.fontory.fontorybe.config.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String accessSecretKey;
    private final String refreshSecretKey;
    private final String provideSecretKey;
    private final String fontCreateServerSecretKey;

    private final long accessTokenValidityMs; // 15 minutes
    private final long refreshTokenValidityMs; // 7 days
    private final long tempTokenValidityMs; // 15 minutes

    private final long accessTokenValiditySec;
    private final long refreshTokenValiditySec;
    private static final long ONE_SECOND = 1000;
    private final String fontCreateServerSubject;

    public JwtProperties(String accessSecretKey,
                         String refreshSecretKey,
                         String provideSecretKey,
                         String fontCreateServerSecretKey,
                         long accessTokenValidityMs,
                         long refreshTokenValidityMs,
                         long tempTokenValidityMs
    ) {
        this.accessSecretKey = accessSecretKey;
        this.refreshSecretKey = refreshSecretKey;
        this.provideSecretKey = provideSecretKey;
        this.fontCreateServerSecretKey = fontCreateServerSecretKey;
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
        this.tempTokenValidityMs = tempTokenValidityMs;
        this.accessTokenValiditySec = accessTokenValidityMs / ONE_SECOND;
        this.refreshTokenValiditySec = refreshTokenValidityMs / ONE_SECOND;
        this.fontCreateServerSubject = "FontCreateServer";
    }
}