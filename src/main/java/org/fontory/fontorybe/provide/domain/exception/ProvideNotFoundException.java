package org.fontory.fontorybe.provide.domain.exception;

public class ProvideNotFoundException extends RuntimeException {
    public ProvideNotFoundException() {
        super("Provide 를 찾을 수 없습니다.");
    }
}
