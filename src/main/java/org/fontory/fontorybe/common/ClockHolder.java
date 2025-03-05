package org.fontory.fontorybe.common;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class ClockHolder {

    public String stampCurrentTimeStamp() {
        return Instant.now().toString();
    }

    public LocalDateTime getCurrentTimeStamp() {
        return LocalDateTime.now();
    }
}
