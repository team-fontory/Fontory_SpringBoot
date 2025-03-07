package org.fontory.fontorybe.provide.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class ProvideNotFoundException extends RuntimeException {
    public ProvideNotFoundException() {
        super("Provide 를 찾을 수 없습니다.");
    }
}
