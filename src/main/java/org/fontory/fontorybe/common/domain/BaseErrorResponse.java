package org.fontory.fontorybe.common.domain;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Getter
@Setter
public class BaseErrorResponse {
    private final String errorMessage;
    private final LocalDateTime timestamp;
    private final String requestId;

    public BaseErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
        this.requestId = MDC.get("requestId");
    }
}
