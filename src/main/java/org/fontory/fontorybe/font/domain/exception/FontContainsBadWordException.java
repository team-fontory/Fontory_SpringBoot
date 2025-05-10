package org.fontory.fontorybe.font.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FontContainsBadWordException extends RuntimeException {
    public FontContainsBadWordException() {
        super("Font contains bad word");
    }
}
