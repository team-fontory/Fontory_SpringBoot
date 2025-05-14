package org.fontory.fontorybe.file.domain.exception;

import org.fontory.fontorybe.common.domain.SkipDiscordNotification;

@SkipDiscordNotification
public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(Long id) {
        super(String.format("File %d Not Found", id));
    }
}
