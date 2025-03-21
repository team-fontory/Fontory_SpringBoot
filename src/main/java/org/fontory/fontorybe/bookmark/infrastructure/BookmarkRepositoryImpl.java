package org.fontory.fontorybe.bookmark.infrastructure;

import jakarta.persistence.EntityManager;
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
}
