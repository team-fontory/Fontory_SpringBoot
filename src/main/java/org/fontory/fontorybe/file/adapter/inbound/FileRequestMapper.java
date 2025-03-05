package org.fontory.fontorybe.file.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.file.adapter.inbound.exception.FileEmptyException;
import org.fontory.fontorybe.file.adapter.inbound.exception.MissingFileExtensionException;
import org.fontory.fontorybe.file.adapter.inbound.exception.UnsupportedFileTypeException;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileRequestMapper {

    private final MemberService memberService;
    private final ProvideService provideService;

    public FileCreate toProfileImageFileCreate(MultipartFile file, Provide provide) {
        validateImageFile(file);
        String generatedFileName = generateFileName(file, provide);

        return FileCreate.builder()
                .file(file)
                .fileType(FileType.PROFILE_IMAGE)
                .fileName(generatedFileName)
                .build();
    }

    public FileCreate toProfileImageFileCreate(MultipartFile file, Long memberId) {
        Member foundMember = memberService.getOrThrowById(memberId);
        Provide provide = provideService.getOrThrownById(foundMember.getProvideId());

        validateImageFile(file);
        String generatedFileName = generateFileName(file, provide);

        return FileCreate.builder()
                .file(file)
                .fileType(FileType.PROFILE_IMAGE)
                .fileName(generatedFileName)
                .build();
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileEmptyException("File is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new MissingFileExtensionException("Valid file extension is missing.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw new UnsupportedFileTypeException("Only jpg, jpeg, png files are allowed.");
        }
    }

    private String generateFileName(MultipartFile file, Provide provide) {
        return provide.getProvidedId() + getFileType(file);
    }

    private String getFileType(MultipartFile file) {
        return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    }
}
