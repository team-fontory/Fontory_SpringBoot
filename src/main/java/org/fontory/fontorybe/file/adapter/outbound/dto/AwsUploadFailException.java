package org.fontory.fontorybe.file.adapter.outbound.dto;

public class AwsUploadFailException extends RuntimeException {
    public AwsUploadFailException(String message) {
        super(message);
    }
}
