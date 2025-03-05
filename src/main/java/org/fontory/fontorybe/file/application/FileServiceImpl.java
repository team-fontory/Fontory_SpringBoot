package org.fontory.fontorybe.file.application;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.adapter.outboud.s3.CloudStorageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final CloudStorageService cloudStorageService;

    @Override
    public FileDetails uploadProfileImage(FileCreate fileCreate) {
        FileMetadata fileMetadata = cloudStorageService.uploadProfileImage(fileCreate);

        return FileDetails.from(fileMetadata);
    }
}
