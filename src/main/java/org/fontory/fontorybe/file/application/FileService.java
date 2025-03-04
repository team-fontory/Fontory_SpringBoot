package org.fontory.fontorybe.file.application;

import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;

public interface FileService {

    FileDetails uploadProfileImage(FileCreate fileCreate);
}
