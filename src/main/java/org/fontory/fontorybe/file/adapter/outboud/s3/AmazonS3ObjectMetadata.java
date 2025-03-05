package org.fontory.fontorybe.file.adapter.outboud.s3;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileMetadata;

import java.time.LocalDateTime;

@Getter
@Builder
public class AmazonS3ObjectMetadata {
    private final String fileName;
    private final String url;
    private final String key;
    private final LocalDateTime requestTime;
    private final long size;

    public static AmazonS3ObjectMetadata from(AmazonS3PutRequest request, String objectUrl) {
        return AmazonS3ObjectMetadata.builder()
                .url(objectUrl)
                .fileName(request.getFileName())
                .key(request.getKey())
                .requestTime(request.getRequestTime())
                .size(request.getSize())
                .build();
    }

    public FileMetadata toModel() {
        return FileMetadata.builder()
                .url(url)
                .key(key)
                .fileName(fileName)
                .size(size)
                .requestTime(requestTime)
                .build();
    }
}
