package org.fontory.fontorybe.file.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fontory.fontorybe.common.domain.BaseEntity;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class FileEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;
    private String fileName;
    private FileType fileType;
    private String extension;
    private String s3Key;
    private Long uploaderId;
    private Long size;
    private LocalDateTime uploadedAt;

    public static FileEntity from(FileMetadata file) {
        return FileEntity.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .extension(file.getExtension())
                .s3Key(file.getKey())
                .uploaderId(file.getUploaderId())
                .size(file.getSize())
                .uploadedAt(file.getUploadedAt())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    public FileMetadata toModel() {
        return FileMetadata.builder()
                .id(this.id)
                .fileName(this.fileName)
                .extension(this.extension)
                .fileType(this.fileType)
                .key(this.s3Key)
                .uploaderId(this.uploaderId)
                .size(this.size)
                .uploadedAt(this.uploadedAt)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
