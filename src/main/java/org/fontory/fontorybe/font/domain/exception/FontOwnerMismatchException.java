package org.fontory.fontorybe.font.domain.exception;

public class FontOwnerMismatchException extends RuntimeException {
    public FontOwnerMismatchException() {
        super("Font owner mismatch");
    }
}
