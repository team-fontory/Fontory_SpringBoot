package org.fontory.fontorybe.file.domain.exception;

public class SingleFileRequiredException extends RuntimeException {
    public SingleFileRequiredException() {
        super("Exactly one file must be uploaded.");
    }
}
