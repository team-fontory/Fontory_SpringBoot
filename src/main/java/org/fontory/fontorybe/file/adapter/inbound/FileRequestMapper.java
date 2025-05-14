package org.fontory.fontorybe.file.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.file.adapter.inbound.exception.FileEmptyException;
import org.fontory.fontorybe.file.adapter.inbound.exception.MissingFileExtensionException;
import org.fontory.fontorybe.file.adapter.inbound.exception.UnsupportedFileTypeException;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileRequestMapper {

    public FileCreate toProfileImageFileCreate(MultipartFile file, Member member) {
        String ext = extractAndValidateExtension(file);
        String generatedFileName = generateProfileImageFileName(member.getId(), ext);

        return FileCreate.builder()
                .file(file)
                .fileType(FileType.PROFILE_IMAGE)
                .fileName(generatedFileName)
                .extension(ext)
                .uploaderId(member.getId())
                .build();
    }

    public FileCreate toFontTemplateImageFileCreate(MultipartFile file, Long memberId) {
        String ext = extractAndValidateExtension(file);
        String generatedFileName = generateProfileImageFileName(memberId,  ext);

        return FileCreate.builder()
                .file(file)
                .fileType(FileType.FONT_PAPER)
                .fileName(generatedFileName)
                .extension(ext)
                .uploaderId(memberId)
                .build();
    }

    private String extractAndValidateExtension(MultipartFile file) {
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
        return extension;
    }

    private String generateProfileImageFileName(Long identifier, String extension) {
        return identifier + "." + extension;
    }
}
