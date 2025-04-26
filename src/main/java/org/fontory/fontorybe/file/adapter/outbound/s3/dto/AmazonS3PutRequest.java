package org.fontory.fontorybe.file.adapter.outbound.s3.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;

@Getter
@Builder
public class AmazonS3PutRequest {
    private final String fileName;
    private final FileType fileType;
    private final String bucketName;
    private final MultipartFile file;
    private final String extension;
    private final Long uploaderId;
    private final String key;
    private final LocalDateTime requestTime;
    private final long size;

    /**
     * for profile-image upload
     */
    public static AmazonS3PutRequest from(FileCreate request,
                                          String key,
                                          String bucketName,
                                          LocalDateTime datestamp) {
        MultipartFile file = request.getFile();

        return AmazonS3PutRequest.builder()
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .file(file)
                .key(key)
                .bucketName(bucketName)
                .extension(request.getExtension())
                .uploaderId(request.getUploaderId())
                .requestTime(datestamp)
                .size(file.getSize())
                .build();
    }

    public PutObjectRequest toPutObjectRequest() {
        return PutObjectRequest.builder()
                .bucket(this.bucketName)
                .key(this.key)
                .contentType(this.getFile().getContentType())
                .acl(ObjectCannedACL.PRIVATE)
                .build();
    }
}
