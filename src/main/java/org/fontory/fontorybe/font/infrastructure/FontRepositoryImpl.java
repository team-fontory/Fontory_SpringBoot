package org.fontory.fontorybe.font.infrastructure;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.fontory.fontorybe.font.service.port.FontRepository;
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
    public List<Font> findAllByMemberId(Long memberId) {
        List<FontEntity> fontEntities = fontJpaRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);

        return fontEntities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }
}
