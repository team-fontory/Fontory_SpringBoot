package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.domain.FileMetadata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeFileRepository implements FileRepository {
    private final AtomicLong authGeneratedID = new AtomicLong(0);
    private final List<FileMetadata> data = new ArrayList<>();

    @Override
    public FileMetadata save(FileMetadata file) {
        LocalDateTime now = LocalDateTime.now();
        if (file.getId() == null || file.getId() == 0) {
            FileMetadata newFile = FileMetadata.builder()
                    .id(authGeneratedID.incrementAndGet())
                    .extension(file.getExtension())
                    .key(file.getKey())
                    .size(file.getSize())
                    .uploaderId(file.getUploaderId())
                    .fileType(file.getFileType())
                    .uploadedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            data.add(newFile);
            return newFile;
        } else {
            data.removeIf(m -> m.getId().equals(file.getId()));
            FileMetadata newFile = FileMetadata.builder()
                    .id(file.getId())
                    .extension(file.getExtension())
                    .key(file.getKey())
                    .size(file.getSize())
                    .uploaderId(file.getUploaderId())
                    .fileType(file.getFileType())
                    .uploadedAt(file.getUploadedAt())
                    .createdAt(file.getCreatedAt())
                    .updatedAt(now)
                    .build();
            data.add(newFile);
            return newFile;
        }
    }

    @Override
    public Optional<FileMetadata> findById(Long id) {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }
}
