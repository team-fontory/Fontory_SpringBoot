package org.fontory.fontorybe.font.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FontOwnerMismatchException extends RuntimeException {
    public FontOwnerMismatchException() {
        super("Font owner mismatch");
    }
}
