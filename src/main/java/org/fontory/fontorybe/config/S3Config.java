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
    private final Map<FileType, String> bucketMap = new EnumMap<>(FileType.class);

    @Getter
    private static String defaultProfileImageUrl = "testUrl";

    @Value("${member.default.profile-image-url}")
    public void setDefaultProfileImageUrl(String defaultProfileImageUrl) {
        S3Config.defaultProfileImageUrl = defaultProfileImageUrl;
    }

    @Autowired
    public S3Config(
            @Value("${spring.cloud.aws.region.static}") String region,
            @Value("${spring.cloud.aws.s3.bucket.profile-image}") String profileImageBucket,
            @Value("${spring.cloud.aws.s3.bucket.font-paper}") String fontPaperBucket
    ) {
        this.region = region;
        this.bucketMap.put(FileType.PROFILE_IMAGE, profileImageBucket);
        this.bucketMap.put(FileType.FONT_PAPER, fontPaperBucket);
    }

    public String getBucketName(FileType fileType) {
        return bucketMap.get(fileType);
    }
}
