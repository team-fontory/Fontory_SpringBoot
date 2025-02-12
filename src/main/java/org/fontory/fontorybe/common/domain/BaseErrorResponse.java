package org.fontory.fontorybe.common.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BaseErrorResponse {
    private final String errorMessage;
    private final LocalDateTime timestamp;

    public BaseErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }
}
