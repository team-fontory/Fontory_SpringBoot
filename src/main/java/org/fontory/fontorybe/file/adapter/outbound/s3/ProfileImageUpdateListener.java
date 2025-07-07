package org.fontory.fontorybe.file.adapter.outbound.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.ProfileImageUpdatedEvent;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.file.infrastructure.ProfileImageDlqRepository;
import org.fontory.fontorybe.file.infrastructure.entity.ProfileImageDlq;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileImageUpdateListener {
    private final S3Client s3;
    private final S3Config s3Config;
    private final ProfileImageDlqRepository profileImageDlqRepository;
    private final MemberUpdateService memberUpdateService;

    private String profileImageBucketName;
    private String profileImagePrefix;

    @PostConstruct
    void init() {
        profileImageBucketName = s3Config.getBucketName(FileType.PROFILE_IMAGE);
        profileImagePrefix = s3Config.getPrefix(FileType.PROFILE_IMAGE);
    }

    /**
     * 1. S3 copy(temp -> fixed)
     * 2. S3 delete(temp)
     * 3. Member profileImageKey update
     */
    @Async
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(propagation = REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(ProfileImageUpdatedEvent event) {
        RetryContext ctx = RetrySynchronizationManager.getContext();
        int attempt = ctx.getRetryCount() + 1;  // 0-based retryCount
        Integer maxAttempts = (Integer) ctx.getAttribute(ctx.MAX_ATTEMPTS);
        if (maxAttempts == null) {
            maxAttempts = 3;
        }

        String tempKey  = event.getTempKey();
        String fixedKey = event.getFixedKey();
        Long memberId   = event.getMemberId();
        String srcKey   = profileImagePrefix + "/" + tempKey;
        String dstKey   = profileImagePrefix + "/" + fixedKey;

        log.info("Promotion attempt {}/{} for memberId={}, tempKey={}, fixedKey={}",
                attempt, maxAttempts, memberId, tempKey, fixedKey);

        // 1. S3 copy(temp -> fixed)
        s3.copyObject(CopyObjectRequest.builder()
                        .sourceBucket(profileImageBucketName)
                        .sourceKey(srcKey)
                        .destinationBucket(profileImageBucketName)
                        .destinationKey(dstKey)
                        .build());
        log.info("S3 copy succeeded: {}/{} → {}/{}",
                profileImageBucketName, srcKey, profileImageBucketName, dstKey);

        // 2. S3 delete(temp)
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(profileImageBucketName)
                .key(srcKey)
                .build());
        log.info("S3 temp object deleted: {}/{}", profileImageBucketName, srcKey);

        // 3. DB update
        Member updatedMember = memberUpdateService.setProfileImageKey(event.getMemberId(), event.getFixedKey());
        log.info("Member profileImageKey updated: memberId={}, newKey={}",
                updatedMember.getId(), updatedMember.getProfileImageKey());
    }

    /**
     * When afterCommit methods fails maxAttempts(3) times, recover process begin
     * 1. record failed event to dql repository
     * 2. discord alert
     */
    @Recover
    public void recover(ProfileImageUpdatedEvent event, Exception e) {
        String tempKey = event.getTempKey();
        String fixedKey = event.getFixedKey();
        Long memberId   = event.getMemberId();

        log.error("Promotion retries exhausted for profile image: memberId={}, tempKey={}, fixedKey={}",
                memberId, tempKey, fixedKey, e);

        log.info("Recording DLQ entry for failed promotion: memberId={}, tempKey={}, fixedKey={}",
                memberId, tempKey, fixedKey);
        profileImageDlqRepository.save(ProfileImageDlq.builder()
                        .memberId(event.getMemberId())
                        .fixKey(event.getFixedKey())
                        .tempKey(event.getTempKey())
                        .time(LocalDateTime.now())
                        .build());
        log.error("Profile image promotion permanently failed, moved to DLQ. " +
                "memberId={}, tempKey={}, fixedKey={}", memberId, tempKey, fixedKey);

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollback(ProfileImageUpdatedEvent event) {
        String tempKey = event.getTempKey();
        String srcKey  = profileImagePrefix + "/" + tempKey;

        log.warn("Transaction rolled back; cleaning up orphan tempKey: {}/{}",
                profileImageBucketName, srcKey);
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(profileImageBucketName)
                    .key(profileImagePrefix + "/" + event.getTempKey())
                    .build());
            log.info("Orphan tempKey deleted after rollback: {}/{}",
                    profileImageBucketName, srcKey);
        } catch (Exception e) {
            log.warn("Failed to delete orphan tempKey after rollback: {}/{} – {}",
                    profileImageBucketName, srcKey, e.getMessage(), e);
        }
    }
}
