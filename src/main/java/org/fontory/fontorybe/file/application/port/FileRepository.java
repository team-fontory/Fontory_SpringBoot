package org.fontory.fontorybe.file.application.port;


import org.fontory.fontorybe.file.domain.FileMetadata;

import java.util.Optional;

public interface FileRepository {
    FileMetadata save(FileMetadata file);
    Optional<FileMetadata> findById(Long id);
}
