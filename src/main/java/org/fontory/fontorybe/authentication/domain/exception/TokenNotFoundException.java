package org.fontory.fontorybe.authentication.domain.exception;

import org.springframework.security.oauth2.jwt.JwtException;

public class TokenNotFoundException extends JwtException {
    public TokenNotFoundException() {
        super("Token not found");
    }
}
