package org.fontory.fontorybe.font.domain.exception;

public class FontNotFoundException extends RuntimeException {
    public FontNotFoundException() {
        super("Font not found");
    }
}
