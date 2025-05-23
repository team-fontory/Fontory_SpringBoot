package org.fontory.fontorybe.font.infrastructure;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FontRepositoryImpl implements FontRepository {
    private final FontJpaRepository fontJpaRepository;
    private final EntityManager em;

    @Override
    public Font save(Font font) {
        FontEntity savedFont = fontJpaRepository.save(FontEntity.from(font));

        em.flush();
        em.refresh(savedFont);

        return savedFont.toModel();
    }

    @Override
    public List<Font> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId) {
        List<FontEntity> fontEntities = fontJpaRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);

        return fontEntities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Font> findById(Long id) {
        return fontJpaRepository.findById(id).map(FontEntity::toModel);
    }

    @Override
    public Page<Font> findAllByMemberIdAndStatus(Long memberId, PageRequest pageRequest, FontStatus status) {
        Page<FontEntity> fontEntityPage = fontJpaRepository.findAllByMemberIdAndStatus(memberId, pageRequest, status);

        return fontEntityPage.map(FontEntity::toModel);
    }

    @Override
    public void deleteById(Long id) {
        fontJpaRepository.deleteById(id);
    }

    @Override
    public Page<Font> findAllByStatus(PageRequest pageRequest, FontStatus status) {
        Page<FontEntity> fontEntityPage = fontJpaRepository.findAllByStatus(pageRequest, status);

        return fontEntityPage.map(FontEntity::toModel);
    }

    @Override
    public Page<Font> findByNameContainingAndStatus(String keyword, PageRequest pageRequest, FontStatus status) {
        Page<FontEntity> fontEntityPage = fontJpaRepository.findByNameContainingAndStatus(keyword, pageRequest, status);

        return fontEntityPage.map(FontEntity::toModel);
    }

    @Override
    public List<Font> findTop3ByMemberIdAndIdNotAndStatusOrderByCreatedAtDesc(Long memberId, Long fontId, FontStatus status) {
        List<FontEntity> fontEntities = fontJpaRepository.findTop3ByMemberIdAndIdNotAndStatusOrderByCreatedAtDesc(memberId, fontId, status);

        return fontEntities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Font> findAllByIdIn(List<Long> ids) {
        List<FontEntity> fontEntities = fontJpaRepository.findAllByIdIn(ids);

        return fontEntities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Font> findTop4ByMemberIdAndStatusOrderByDownloadAndBookmarkCountDesc(Long memberId, FontStatus status) {
        Pageable topFour = PageRequest.of(0, 4);
        List<FontEntity> entities = fontJpaRepository.findTopByMemberIdAndStatusOrderByPopularityDesc(memberId, status, topFour);

        return entities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Font> findTop3ByStatusOrderByDownloadAndBookmarkCountDesc(FontStatus status) {
        Pageable topThree = PageRequest.of(0, 3);
        List<FontEntity> entities = fontJpaRepository.findTop3ByStatusOrderByDownloadAndBookmarkCountDesc(status, topThree);

        return entities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String fontName) {
        return fontJpaRepository.existsByName(fontName);
    }
}
