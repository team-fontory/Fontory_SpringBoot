package org.fontory.fontorybe.file.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outboud.s3.ProfileImageUpdatedEvent;
import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.adapter.outboud.port.CloudStorageService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final MemberRepository memberRepository;
    private final CloudStorageService cloudStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public FileMetadata getOrThrowById(Long id) {
        return Optional.ofNullable(id)
                .flatMap(fileRepository::findById)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    @Transactional
    public FileUploadResult uploadProfileImage(FileCreate fileCreate, Member requestMember) {
        boolean isInitial = S3Config.getDefaultProfileImageUrl().equals(requestMember.getProfileImageKey());
        String fixedKey = isInitial
                ? UUID.randomUUID().toString()
                : requestMember.getProfileImageKey();

        String tempKey = UUID.randomUUID().toString();

        FileMetadata metadata = cloudStorageService.uploadProfileImage(fileCreate, tempKey);
        FileMetadata savedMetaData = fileRepository.save(metadata);

        Member profileImageUpdatedMember = requestMember.setProfileImageKey(fixedKey);
        memberRepository.save(profileImageUpdatedMember);

        eventPublisher.publishEvent(new ProfileImageUpdatedEvent(tempKey, fixedKey));
        String fileUrl = cloudStorageService.getFileUrl(savedMetaData, fixedKey);

        return FileUploadResult.from(savedMetaData, fileUrl);
    }

    @Override
    @Transactional
    public FileUploadResult uploadFontTemplateImage(FileCreate fileCreate) {
        FileMetadata fileMetadata = cloudStorageService.uploadFontTemplateImage(fileCreate);
        FileMetadata savedMetaData = fileRepository.save(fileMetadata);
        String fileUrl = cloudStorageService.getFileUrl(savedMetaData, savedMetaData.getKey());
        return FileUploadResult.from(fileMetadata, fileUrl);
    }
}
