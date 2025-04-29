package org.fontory.fontorybe.bookmark.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.bookmark.controller.dto.BookmarkDeleteResponse;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkAlreadyException;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkNotFoundException;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final FontRepository fontRepository;
    private final MemberLookupService memberLookupService;
    private final FontService fontService;

    @Override
    @Transactional
    public Bookmark create(Long memberId, Long fontId) {
        if (bookmarkRepository.existsByMemberIdAndFontId(memberId, fontId)) {
            throw new BookmarkAlreadyException();
        }

        Member member = memberLookupService.getOrThrowById(memberId);
        Font font = fontService.getOrThrowById(fontId);

        font.increaseBookmarkCount();
        fontRepository.save(font);

        return bookmarkRepository.save(Bookmark.from(memberId, fontId));
    }

    @Override
    @Transactional
    public BookmarkDeleteResponse delete(Long memberId, Long fontId) {
        Member member = memberLookupService.getOrThrowById(memberId);
        Font font = fontService.getOrThrowById(fontId);

        Bookmark bookmark = bookmarkRepository.findByMemberIdAndFontId(memberId, fontId)
                .orElseThrow(BookmarkNotFoundException::new);

        bookmarkRepository.deleteById(bookmark.getId());

        font.decreaseBookmarkCount();
        fontRepository.save(font);

        return BookmarkDeleteResponse.from(bookmark.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FontResponse> getBookmarkedFonts(Long memberId, int page, int size, String keyword) {
        Member member = memberLookupService.getOrThrowById(memberId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Bookmark> bookmarks = bookmarkRepository.findAllByMemberId(memberId, pageRequest);

        List<Long> fontIds = bookmarks.stream()
                .map(Bookmark::getFontId)
                .toList();

        List<Font> fonts = fontRepository.findAllByIdIn(fontIds);

        List<FontResponse> filtered = fonts.stream()
                .filter(font -> !StringUtils.hasText(keyword) || font.getName().contains(keyword))
                .map(font -> FontResponse.from(font, true))
                .toList();

        return new PageImpl<>(filtered, pageRequest, bookmarks.getTotalElements());
    }
}
