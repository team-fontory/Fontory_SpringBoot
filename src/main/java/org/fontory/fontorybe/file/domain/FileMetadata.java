package org.fontory.fontorybe.file.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileMetadata {
    private final String fileName;
    private final String url;
    private final String key;
    private final LocalDateTime requestTime;
    private final long size;
}
