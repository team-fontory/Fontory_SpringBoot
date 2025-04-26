package org.fontory.fontorybe.file.application.port;

import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.member.domain.Member;

public interface FileService {

    FileUploadResult uploadProfileImage(FileCreate fileCreate, Member requestMember);
    FileMetadata getOrThrowById(Long id);
    FileUploadResult uploadFontTemplateImage(FileCreate fileCreate);
}
