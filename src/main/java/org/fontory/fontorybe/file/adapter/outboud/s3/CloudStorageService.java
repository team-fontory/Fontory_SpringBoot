package org.fontory.fontorybe.file.adapter.outboud.s3;

import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;

public interface CloudStorageService {

    FileMetadata uploadProfileImage(FileCreate fileCreate);
}
