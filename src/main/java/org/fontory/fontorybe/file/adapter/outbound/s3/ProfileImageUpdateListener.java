package org.fontory.fontorybe.file.adapter.outbound.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.ProfileImageUpdatedEvent;
import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileImageUpdateListener {
    private final S3Client s3;
    private final S3Config s3Config;
    private String profileImageBucketName;
    private String profileImagePrefix;

    @PostConstruct
    void init() {
        profileImageBucketName = s3Config.getBucketName(FileType.PROFILE_IMAGE);
        profileImagePrefix = s3Config.getPrefix(FileType.PROFILE_IMAGE);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommit(ProfileImageUpdatedEvent event) {
        s3.copyObject(CopyObjectRequest.builder()
                        .sourceBucket(profileImageBucketName)
                        .sourceKey(profileImagePrefix + "/" + event.getTempKey())
                        .destinationBucket(profileImageBucketName)
                        .destinationKey(profileImagePrefix + "/" + event.getFixedKey())
                        .build());

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(profileImageBucketName)
                .key(profileImagePrefix + "/" + event.getTempKey())
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollback(ProfileImageUpdatedEvent event) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(profileImageBucketName)
                    .key(profileImagePrefix + "/" + event.getTempKey())
                    .build());
        } catch (Exception e) {
            log.warn("Rollback profile image failed", e);
        }
    }
}
