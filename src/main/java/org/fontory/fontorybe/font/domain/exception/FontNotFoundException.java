package org.fontory.fontorybe.font.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FontNotFoundException extends RuntimeException {
    public FontNotFoundException() {
        super("Font not found");
    }
}
