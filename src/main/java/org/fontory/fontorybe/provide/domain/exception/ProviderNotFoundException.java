package org.fontory.fontorybe.provide.domain.exception;

public class ProviderNotFoundException extends RuntimeException{
    public ProviderNotFoundException(String provider) {
        super("Unknown provider: " + provider);
    }
}
