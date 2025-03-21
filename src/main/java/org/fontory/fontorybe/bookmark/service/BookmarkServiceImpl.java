package org.fontory.fontorybe.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkAlreadyException;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final FontRepository fontRepository;

    private final MemberService memberService;
    private final FontService fontService;

    @Override
    @Transactional
    public Bookmark create(Long memberId, Long fontId) {
        if (bookmarkRepository.existsByMemberIdAndFontId(memberId, fontId)) {
            throw new BookmarkAlreadyException();
        }

        Member member = memberService.getOrThrowById(memberId);
        Font font = fontService.getOrThrowById(fontId);

        font.increaseBookmarkCount();
        fontRepository.save(font);

        return bookmarkRepository.save(Bookmark.from(memberId, fontId));
    }
}
