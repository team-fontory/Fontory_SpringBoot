package org.fontory.fontorybe.config;

import lombok.Getter;
import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Configuration
public class S3Config {

    private final String region;
    private final String cdnUrl;
    private final Map<FileType, String> bucketMap = new EnumMap<>(FileType.class);
    private final Map<FileType, String> prefixMap = new EnumMap<>(FileType.class);

    @Autowired
    public S3Config(
            @Value("${spring.cloud.aws.region.static}") String region,
            @Value("${url.cdn}") String cdnUrl,
            @Value("${spring.cloud.aws.s3.bucket.profile-image}") String profileImageBucket,
            @Value("${spring.cloud.aws.s3.bucket.font-paper}") String fontPaperBucket,
            @Value("${spring.cloud.aws.s3.bucket.profile-image.prefix}") String profileImageBucketPrefix,
            @Value("${spring.cloud.aws.s3.bucket.font-paper.prefix}") String fontPaperBucketPrefix
    ) {
        this.region = region;
        this.cdnUrl = cdnUrl;
        this.bucketMap.put(FileType.PROFILE_IMAGE, profileImageBucket);
        this.bucketMap.put(FileType.FONT_PAPER, fontPaperBucket);
        this.prefixMap.put(FileType.PROFILE_IMAGE, profileImageBucketPrefix);
        this.prefixMap.put(FileType.FONT_PAPER, fontPaperBucketPrefix);
    }

    public String getBucketName(FileType fileType) {
        return bucketMap.get(fileType);
    }

    public String getPrefix(FileType fileType) {
        return prefixMap.get(fileType);
    }
}
