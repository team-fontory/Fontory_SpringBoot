package org.fontory.fontorybe.file.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<FileEntity, Long> {
}
