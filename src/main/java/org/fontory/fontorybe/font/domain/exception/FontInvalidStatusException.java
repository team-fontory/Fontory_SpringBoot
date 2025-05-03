package org.fontory.fontorybe.font.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FontInvalidStatusException extends RuntimeException {
    public FontInvalidStatusException() {
        super("Font status is invalid");
    }
}
