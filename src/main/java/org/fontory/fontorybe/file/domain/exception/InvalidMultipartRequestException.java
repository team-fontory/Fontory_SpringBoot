package org.fontory.fontorybe.file.domain.exception;

public class InvalidMultipartRequestException extends RuntimeException {
    public InvalidMultipartRequestException() {
        super("The request is not in multipart format.");
    }
}
