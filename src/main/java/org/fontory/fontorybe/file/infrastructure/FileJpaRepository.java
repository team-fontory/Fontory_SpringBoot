package org.fontory.fontorybe.file.infrastructure;

import org.fontory.fontorybe.file.infrastructure.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<FileEntity, Long> {
}
