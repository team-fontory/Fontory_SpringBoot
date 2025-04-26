package org.fontory.fontorybe.file.application;

import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class AWSBucketMapper {
    private final Map<FileType, String> bucketMap = new EnumMap<>(FileType.class);

    @Autowired
    public AWSBucketMapper(
            @Value("${spring.cloud.aws.s3.bucket.profile-image}") String profileImageBucket,
            @Value("${spring.cloud.aws.s3.bucket.font-paper}") String fontPaperBucket
    ) {
        bucketMap.put(FileType.PROFILE_IMAGE, profileImageBucket);
        bucketMap.put(FileType.FONT_PAPER, fontPaperBucket);
    }

    public String getBucketName(FileType fileType) {
        return bucketMap.get(fileType);
    }
}
