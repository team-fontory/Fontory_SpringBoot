package org.fontory.fontorybe.file.adapter.outboud.s3;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Builder
public class AmazonS3PutRequest {
    private final MultipartFile file;
    private final String fileName;
    private final String key;
    private final LocalDateTime requestTime;
    private final long size;

    /**
     * for profile-image upload
     */
    public static AmazonS3PutRequest from(FileCreate request, LocalDateTime datestamp) {
        MultipartFile file = request.getFile();

        return AmazonS3PutRequest.builder()
                .file(file)
                .key(request.getFileName())
                .requestTime(datestamp)
                .size(file.getSize())
                .build();
    }

    /**
     * for font-paper upload
     */
//    public static AmazonS3PutRequest from(FileCreate request, LocalDateTime datestamp, FontInfo fontInfo) {
//        MultipartFile file = request.getFile();
//        String key = fontInfo.getMemberId() + "/" + fontInfo.getFontName() + "/" + request.getFileName();
//        return AmazonS3PutRequest.builder()
//                .file(file)
//                .key(request.getFileName())
//                .requestTime(datestamp)
//                .size(file.getSize())
//                .build();
//    }
}
