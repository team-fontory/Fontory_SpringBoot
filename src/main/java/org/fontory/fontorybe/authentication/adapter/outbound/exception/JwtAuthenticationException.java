package org.fontory.fontorybe.authentication.adapter.outbound.exception;


import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(String msg) {
        super(msg);
    }
    public JwtAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }
}
