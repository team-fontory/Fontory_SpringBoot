package org.fontory.fontorybe.file.infrastructure;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.infrastructure.entity.FileEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {
    private final FileJpaRepository fileJpaRepository;
    private final EntityManager em;

    @Override
    public FileMetadata save(FileMetadata file) {
        FileEntity savedFile = fileJpaRepository.save(FileEntity.from(file));

        /**
         * For Adapt BaseEntity
         */
        em.flush();
        em.refresh(savedFile);

        return savedFile.toModel();
    }

    @Override
    public Optional<FileMetadata> findById(Long id) {
        return fileJpaRepository.findById(id).map(FileEntity::toModel);
    }
}
