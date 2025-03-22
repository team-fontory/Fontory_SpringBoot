package org.fontory.fontorybe.font.service.port;

import java.util.List;
import java.util.Optional;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface FontRepository {
    Font save(Font font);
    List<Font> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
    Optional<Font> findById(Long id);
    Page<Font> findAllByMemberId(Long memberId, PageRequest pageRequest);
    void deleteById(Long id);
    Page<Font> findAll(PageRequest pageRequest);
    Page<Font> findByNameContaining(String keyword, PageRequest pageRequest);
    List<Font> findTop3ByMemberIdAndIdNotOrderByCreatedAtDesc(Long memberId, Long fontId);
    List<Font> findAllByIdIn(List<Long> ids);
    List<Font> findTop4ByMemberIdOrderByDownloadAndBookmarkCountDesc(Long memberId);
    List<Font> findTop3OrderByDownloadAndBookmarkCountDesc();
}
