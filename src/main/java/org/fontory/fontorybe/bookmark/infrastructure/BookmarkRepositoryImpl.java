package org.fontory.fontorybe.bookmark.infrastructure;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.bookmark.infrastructure.entity.BookmarkEntity;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryImpl implements BookmarkRepository {
    private final BookmarkJpaRepository bookmarkJpaRepository;
    private final EntityManager em;

    @Override
    public Bookmark save(Bookmark bookmark) {
        BookmarkEntity savedBookmark = bookmarkJpaRepository.save(BookmarkEntity.from(bookmark));

        em.flush();
        em.refresh(savedBookmark);

        return savedBookmark.toModel();
    }

    @Override
    public boolean existsByMemberIdAndFontId(Long memberId, Long fontId) {
        return bookmarkJpaRepository.existsByMemberIdAndFontId(memberId, fontId);
    }

    @Override
    public Optional<Bookmark> findByMemberIdAndFontId(Long memberId, Long fontId) {
        return bookmarkJpaRepository.findByMemberIdAndFontId(memberId, fontId)
                .map(BookmarkEntity::toModel);
    }

    @Override
    public void deleteById(Long id) {
        bookmarkJpaRepository.deleteById(id);
    }
}
