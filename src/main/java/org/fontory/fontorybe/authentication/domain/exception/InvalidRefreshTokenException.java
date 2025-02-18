package org.fontory.fontorybe.authentication.domain.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token");
    }
}
