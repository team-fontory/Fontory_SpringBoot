package org.fontory.fontorybe.font.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FontDuplicateNameExistsException extends RuntimeException {
    public FontDuplicateNameExistsException() {
        super("Font name is duplicate");
    }
}
