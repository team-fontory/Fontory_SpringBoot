package org.fontory.fontorybe.file.adapter.outbound.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileJpaRepository extends JpaRepository<FileEntity, Long> {
    
    // 파일 조회 최적화 쿼리
    @Query("SELECT f FROM FileEntity f WHERE f.uploaderId = :uploaderId ORDER BY f.createdAt DESC")
    Page<FileEntity> findByUploaderIdOrderByCreatedAtDesc(@Param("uploaderId") Long uploaderId, Pageable pageable);
    
    @Query(value = "SELECT * FROM file WHERE file_type = :fileType ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<FileEntity> findRecentByFileType(@Param("fileType") String fileType, @Param("limit") int limit);
    
    @Query("SELECT COUNT(f) FROM FileEntity f WHERE f.uploaderId = :uploaderId")
    long countByUploaderId(@Param("uploaderId") Long uploaderId);
}
