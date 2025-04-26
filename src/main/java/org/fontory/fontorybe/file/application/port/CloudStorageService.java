package org.fontory.fontorybe.file.application.port;

import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;

public interface CloudStorageService {

    FileMetadata uploadProfileImage(FileCreate fileCreate, String key);
    String getFileUrl(FileMetadata fileMetadata, String key);
    FileMetadata uploadFontTemplateImage(FileCreate request);
}
