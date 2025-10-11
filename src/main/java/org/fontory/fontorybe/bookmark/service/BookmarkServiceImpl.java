package org.fontory.fontorybe.bookmark.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.bookmark.controller.dto.BookmarkDeleteResponse;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkAlreadyException;
import org.fontory.fontorybe.bookmark.domain.exception.BookmarkNotFoundException;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
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

/**
 * 북마크 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 폰트의 북마크 추가, 삭제 및 북마크한 폰트 목록 조회 기능 제공
 */
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final FontRepository fontRepository;
    private final MemberLookupService memberLookupService;
    private final FontService fontService;
    private final CloudStorageService cloudStorageService;

    /**
     * 폰트를 북마크에 추가
     * 북마크 추가 시 해당 폰트의 북마크 카운트를 증가
     * 
     * @param memberId 북마크를 추가하는 회원 ID
     * @param fontId 북마크할 폰트 ID
     * @return 생성된 북마크 엔티티
     * @throws BookmarkAlreadyException 이미 북마크된 폰트인 경우
     */
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

    /**
     * 북마크를 삭제
     * 북마크 삭제 시 해당 폰트의 북마크 카운트를 감소
     * 
     * @param memberId 북마크를 삭제하는 회원 ID
     * @param fontId 북마크를 해제할 폰트 ID
     * @return 삭제 결과
     * @throws BookmarkNotFoundException 북마크가 존재하지 않는 경우
     */
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

    /**
     * 회원이 북마크한 폰트 목록을 페이지네이션과 함께 조회
     * 키워드 검색 기능 포함 (폰트 이름 기준)
     * 
     * @param memberId 조회할 회원 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param keyword 검색 키워드 (null 또는 빈 문자열일 경우 전체 조회)
     * @return 북마크한 폰트 목록 (페이지네이션 적용)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FontResponse> getBookmarkedFonts(Long memberId, int page, int size, String keyword) {
        Member member = memberLookupService.getOrThrowById(memberId);

        // 키워드가 없으면 일반 페이지네이션 사용
        if (!StringUtils.hasText(keyword)) {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
            Page<Bookmark> bookmarks = bookmarkRepository.findAllByMemberId(memberId, pageRequest);

            List<Long> fontIds = bookmarks.stream()
                    .map(Bookmark::getFontId)
                    .toList();

            List<Font> fonts = fontRepository.findAllByIdIn(fontIds);

            List<FontResponse> fontResponses = fonts.stream()
                    .map(font -> {
                        Member writer = memberLookupService.getOrThrowById(font.getMemberId());
                        String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                        return FontResponse.from(font, true, writer.getNickname(), woff2Url);
                    })
                    .toList();

            return new PageImpl<>(fontResponses, pageRequest, bookmarks.getTotalElements());
        }

        // With keyword, need to filter all bookmarks first, then paginate
        // Get all bookmarks for the member (no pagination)
        PageRequest allBookmarksRequest = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Order.desc("createdAt")));
        Page<Bookmark> allBookmarks = bookmarkRepository.findAllByMemberId(memberId, allBookmarksRequest);
        
        List<Long> allFontIds = allBookmarks.stream()
                .map(Bookmark::getFontId)
                .toList();

        List<Font> allFonts = fontRepository.findAllByIdIn(allFontIds);

        // Filter by keyword
        List<Font> filteredFonts = allFonts.stream()
                .filter(font -> font.getName().contains(keyword))
                .toList();

        // Apply manual pagination
        int start = page * size;
        int end = Math.min(start + size, filteredFonts.size());
        
        List<FontResponse> pageContent = filteredFonts.subList(
                Math.min(start, filteredFonts.size()),
                end
        ).stream()
                .map(font -> {
                    Member writer = memberLookupService.getOrThrowById(font.getMemberId());
                    String woff2Url = cloudStorageService.getWoff2Url(font.getKey());
                    return FontResponse.from(font, true, writer.getNickname(), woff2Url);
                })
                .toList();

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        return new PageImpl<>(pageContent, pageRequest, filteredFonts.size());
    }
}
