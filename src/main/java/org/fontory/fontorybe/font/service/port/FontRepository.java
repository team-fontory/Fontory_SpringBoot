package org.fontory.fontorybe.font.service.port;

import java.util.List;
import java.util.Optional;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FontRepository {
    Font save(Font font);
    List<Font> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<Font> findById(Long id);
    Page<Font> findAllByMemberIdAndStatus(Long memberId, PageRequest pageRequest, FontStatus status);
    void deleteById(Long id);
    Page<Font> findAllByStatus(PageRequest pageRequest, FontStatus status);
    Page<Font> findByNameContainingAndStatus(String keyword, PageRequest pageRequest, FontStatus status);
    List<Font> findTop3ByMemberIdAndIdNotAndStatusOrderByCreatedAtDesc(Long memberId, Long fontId, FontStatus status);
    List<Font> findAllByIdIn(List<Long> ids);
    List<Font> findTop4ByMemberIdAndStatusOrderByDownloadAndBookmarkCountDesc(Long memberId, FontStatus status);
    List<Font> findTop3ByStatusOrderByDownloadAndBookmarkCountDesc(FontStatus status);
    boolean existsByName(String fontName);
}
