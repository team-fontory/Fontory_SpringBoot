package org.fontory.fontorybe.file.application;

import java.util.Optional;
import java.util.UUID;

import org.fontory.fontorybe.file.adapter.inbound.FileRequestMapper;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.ProfileImageUpdatedEvent;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.domain.exception.FileNotFoundException;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final MemberDefaults memberDefaults;
    private final FileRepository fileRepository;
    private final FileRequestMapper fileRequestMapper;
    private final CloudStorageService cloudStorageService;
    private final MemberLookupService memberLookupService;
    private final MemberUpdateService memberUpdateService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public FileMetadata getOrThrowById(Long id) {
        return Optional.ofNullable(id)
                .flatMap(fileRepository::findById)
                .orElseThrow(() -> new FileNotFoundException(id));
    }

    @Override
    @Transactional
    public FileUploadResult uploadProfileImage(MultipartFile file, Long memberId) {
        log.info("Processing profile image upload: fileName={}, memberId={}", file.getOriginalFilename(), memberId);

        Member requestMember = memberLookupService.getOrThrowById(memberId);
        FileCreate profileImageFileCreate = fileRequestMapper.toProfileImageFileCreate(file, requestMember);
        boolean isInitial = memberDefaults.getProfileImageKey().equals(requestMember.getProfileImageKey());
        String fixedKey = isInitial
                ? UUID.randomUUID().toString()
                : requestMember.getProfileImageKey();

        String tempKey = UUID.randomUUID().toString();

        log.info("Uploading profile image to cloud storage: memberId={}, tempKey={}", memberId, tempKey);
        FileMetadata metadata = cloudStorageService.uploadProfileImage(profileImageFileCreate, tempKey);
        metadata.updateKey(fixedKey);
        FileMetadata savedMetaData = fileRepository.save(metadata);
        Member updated = memberUpdateService.setProfileImageKey(requestMember, fixedKey);

        log.info("Updating member profile image key: memberId={}, memberProfileImageKey={}", updated.getId(), updated.getProfileImageKey());
        log.info("Publishing image update event: tempKey={}, fixedKey={}", tempKey, fixedKey);
        eventPublisher.publishEvent(new ProfileImageUpdatedEvent(tempKey, fixedKey));
        String fileUrl = cloudStorageService.getProfileImageUrl(fixedKey);
        FileUploadResult result = FileUploadResult.from(savedMetaData, fileUrl);
        log.info("Profile image upload completed successfully: memberId={}, fileUrl={}", memberId, fileUrl);
        return result;
    }

    @Override
    @Transactional
    public FileUploadResult uploadFontTemplateImage(MultipartFile file, Long memberId) {
        log.info("Processing font template image upload: fileName={}, memberId={}", file.getOriginalFilename(), memberId);
        Member member = memberLookupService.getOrThrowById(memberId);
        FileCreate fontTemplateImageFileCreate = fileRequestMapper.toFontTemplateImageFileCreate(file, member.getId());
        log.info("Uploading font template image to cloud storage: memberId={}", memberId);
        FileMetadata fileMetadata = cloudStorageService.uploadFontTemplateImage(fontTemplateImageFileCreate);
        FileMetadata savedMetaData = fileRepository.save(fileMetadata);
        String fileUrl = cloudStorageService.getFontPaperUrl(savedMetaData.getKey());
        FileUploadResult result = FileUploadResult.from(savedMetaData, fileUrl);
        log.info("Font template image upload completed successfully: memberId={}, fileUrl={}", memberId, fileUrl);
        return result;
    }
}
