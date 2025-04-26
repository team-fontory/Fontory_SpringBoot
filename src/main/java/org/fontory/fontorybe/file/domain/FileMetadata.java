package org.fontory.fontorybe.file.domain;

import lombok.*;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3ObjectMetadata;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileMetadata {
    private Long id;
    private String fileName;
    private FileType fileType;
    private String extension;
    private String key;
    private Long uploaderId;
    private Long size;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FileMetadata from(AmazonS3ObjectMetadata metadata) {
        return FileMetadata.builder()
                .fileName(metadata.getFileName())
                .fileType(metadata.getFileType())
                .extension(metadata.getExtension())
                .key(metadata.getKey())
                .uploaderId(metadata.getUploaderId())
                .size(metadata.getSize())
                .uploadedAt(metadata.getRequestTime())
                .build();
    }

    public FileMetadata update(AmazonS3ObjectMetadata metadata) {
        return FileMetadata.builder()
                // tobe updated
                .fileName(metadata.getFileName())
                .extension(metadata.getExtension())
                .size(metadata.getSize())
                .uploadedAt(metadata.getRequestTime())

                // not to be updated
                .id(this.id)
                .key(this.getKey())
                .fileType(this.getFileType())
                .uploaderId(this.getUploaderId())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
