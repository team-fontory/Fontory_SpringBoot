package org.fontory.fontorybe.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BaseErrorResponse {
    private final String errorMessage;
    private final LocalDateTime timestamp;

    public BaseErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }
}
