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

/**
 * Font 도메인과 데이터베이스 간의 매핑을 처리하는 레포지토리 구현체
 * JPA Entity와 도메인 모델 간의 변환을 담당하고 데이터 지속성을 관리
 */
@Repository
@RequiredArgsConstructor
public class FontRepositoryImpl implements FontRepository {
    private final FontJpaRepository fontJpaRepository;
    private final EntityManager em;

    /**
     * 폰트 엔티티를 데이터베이스에 저장
     * flush와 refresh를 통해 데이터베이스의 최신 상태를 반영 (예: auto_increment ID)
     * 
     * @param font 저장할 Font 도메인 모델
     * @return 저장된 Font 도메인 모델 (ID 포함)
     */
    @Override
    public Font save(Font font) {
        FontEntity savedFont = fontJpaRepository.save(FontEntity.from(font));

        em.flush();
        em.refresh(savedFont);

        return savedFont.toModel();
    }

    /**
     * 특정 회원의 최근 생성된 폰트 5개를 조회
     * 주로 폰트 제작 진행 상태를 보여주기 위해 사용
     * 
     * @param memberId 조회할 회원 ID
     * @return 최근 생성된 폰트 목록 (최대 5개)
     */
    @Override
    public List<Font> findTop10ByMemberIdOrderByCreatedAtDesc(Long memberId) {
        List<FontEntity> fontEntities = fontJpaRepository.findTop10ByMemberIdOrderByCreatedAtDesc(memberId);

        return fontEntities.stream()
                .map(FontEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * ID로 폰트를 조회
     * 
     * @param id 조회할 폰트 ID
     * @return 폰트 Optional 객체
     */
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
    
    @Override
    public boolean existsByMemberIdAndName(Long memberId, String fontName) {
        return fontJpaRepository.existsByMemberIdAndName(memberId, fontName);
    }
}
