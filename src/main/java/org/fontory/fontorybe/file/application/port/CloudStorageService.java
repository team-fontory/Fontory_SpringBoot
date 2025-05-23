package org.fontory.fontorybe.file.application.port;

import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;

public interface CloudStorageService {
    FileMetadata uploadProfileImage(FileCreate fileCreate, String key);
    FileMetadata uploadFontTemplateImage(FileCreate request);
    String getProfileImageUrl(String key);
    String getFontPaperUrl(String key);
    String getWoff2Url(String key);
    String getTtfUrl(String key);
}
