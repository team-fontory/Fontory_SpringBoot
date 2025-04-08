package org.fontory.fontorybe.font.service;

import java.util.List;
import java.util.stream.Collectors;

import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FontServiceImpl implements FontService {
    private final FontRepository fontRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberService memberService;

    @Override
    @Transactional
    public Font create(Long memberId, FontCreateDTO fontCreateDTO) {
        log.info("Service executing: Creating font for member ID: {}, font name: {}", memberId, fontCreateDTO.getName());
        Member member = memberService.getOrThrowById(memberId);

        Font savedFont = fontRepository.save(Font.from(fontCreateDTO, member.getId()));
        log.info("Service completed: Font created with ID: {}", savedFont.getId());
        return savedFont;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontProgressResponse> getFontProgress(Long memberId) {
        log.info("Service executing: Fetching font progress for member ID: {}", memberId);
        List<Font> fonts = fontRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);
        log.debug("Service detail: Found {} fonts for progress display", fonts.size());

        List<FontProgressResponse> result = fonts.stream()
                .map(FontProgressResponse::from)
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} font progress items", result.size());
        return result;
    }

    @Override
    @Transactional
    public Font update(Long memberId, Long fontId, FontUpdateDTO fontUpdateDTO) {
        log.info("Service executing: Updating font ID: {} for member ID: {}", fontId, memberId);
        Member member = memberService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontOwnership(member.getId(), targetFont.getMemberId());
        
        Font updatedFont = fontRepository.save(targetFont.update(fontUpdateDTO));
        log.info("Service completed: Font ID: {} updated successfully", fontId);
        return updatedFont;
    }

    @Override
    @Transactional(readOnly = true)
    public Font getOrThrowById(Long id) {
        log.debug("Service executing: Fetching font with ID: {}", id);
        return fontRepository.findById(id).orElseThrow(() -> {
            log.error("Service error: Font not found with ID: {}", id);
            return new FontNotFoundException();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FontResponse> getFonts(Long memberId, int page, int size) {
        log.info("Service executing: Fetching fonts for member ID: {}, page: {}, size: {}", memberId, page, size);
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Font> fontPage = fontRepository.findAllByMemberId(memberId, pageRequest);
        log.debug("Service detail: Found {} fonts on page {} for member ID: {}", 
                fontPage.getNumberOfElements(), page, memberId);

        Page<FontResponse> result = fontPage.map(font -> {
            boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, font.getId());
            return FontResponse.from(font, bookmarked);
        });
        
        log.info("Service completed: Retrieved {} fonts out of {} total for member ID: {}", 
                result.getNumberOfElements(), result.getTotalElements(), memberId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public FontDetailResponse getFont(Long fontId) {
        log.info("Service executing: Fetching font details for font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);
        Member member = memberService.getOrThrowById(targetFont.getMemberId());

        FontDetailResponse response = FontDetailResponse.from(targetFont, member.getNickname());
        log.info("Service completed: Retrieved font details for font ID: {} with name: {}", 
                fontId, targetFont.getName());
        return response;
    }

    @Override
    @Transactional
    public FontDeleteResponse delete(Long memberId, Long fontId) {
        log.info("Service executing: Deleting font ID: {} for member ID: {}", fontId, memberId);
        Member member = memberService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontOwnership(member.getId(), targetFont.getMemberId());

        fontRepository.deleteById(targetFont.getId());
        log.info("Service completed: Font ID: {} deleted successfully", fontId);

        return FontDeleteResponse.from(fontId);
    }

    @Override
    public Page<FontPageResponse> getFontPage(Long memberId, int page, int size, String sortBy, String keyword) {
        log.info("Service executing: Fetching font page with memberId: {}, page: {}, size: {}, sortBy: {}, keyword: {}", 
                memberId, page, size, sortBy, keyword);
                
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        if ("downloadCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Order.desc("downloadCount"));
        } else if ("bookmarkCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Order.desc("bookmarkCount"));
        }

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Font> fontPage;
        if (!StringUtils.hasText(keyword)) {
            fontPage = fontRepository.findAll(pageRequest);
        } else {
            log.debug("Service detail: Searching fonts with keyword: {}", keyword);
            fontPage = fontRepository.findByNameContaining(keyword, pageRequest);
        }
        
        log.debug("Service detail: Found {} fonts on page {}", fontPage.getNumberOfElements(), page);

        Page<FontPageResponse> result;
        if (memberId == null) {
            result = fontPage.map(font -> {
                Member member = memberService.getOrThrowById(font.getMemberId());
                return FontPageResponse.from(font, member.getNickname(), false);
            });
        } else {
            result = fontPage.map(font -> {
                Member member = memberService.getOrThrowById(font.getMemberId());
                boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, font.getId());
                return FontPageResponse.from(font, member.getNickname(), bookmarked);
            });
        }
        
        log.info("Service completed: Retrieved {} fonts out of {} total, {} pages", 
                result.getNumberOfElements(), result.getTotalElements(), result.getTotalPages());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getOtherFonts(Long fontId) {
        log.info("Service executing: Fetching other fonts from the same creator for font ID: {}", fontId);
        Font font = getOrThrowById(fontId);
        Member member = memberService.getOrThrowById(font.getMemberId());

        List<Font> fonts = fontRepository.findTop3ByMemberIdAndIdNotOrderByCreatedAtDesc(member.getId(), fontId);
        log.debug("Service detail: Found {} other fonts from the same creator", fonts.size());

        List<FontResponse> result = fonts.stream()
                .map(f -> {
                    boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), f.getId());
                    return FontResponse.from(f, bookmarked);
                })
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} other fonts from creator of font ID: {}", result.size(), fontId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getMyPopularFonts(Long memberId) {
        log.info("Service executing: Fetching popular fonts for member ID: {}", memberId);
        Member member = memberService.getOrThrowById(memberId);

        List<Font> fonts = fontRepository.findTop4ByMemberIdOrderByDownloadAndBookmarkCountDesc(memberId);
        log.debug("Service detail: Found {} popular fonts for member ID: {}", fonts.size(), memberId);

        List<FontResponse> result = fonts.stream()
                .map(f -> {
                    boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), f.getId());
                    return FontResponse.from(f, bookmarked);
                })
                .collect(Collectors.toList());
                
        log.info("Service completed: Retrieved {} popular fonts for member ID: {}", result.size(), memberId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getPopularFonts(Long memberId) {
        log.info("Service executing: Fetching global popular fonts, requesting member ID: {}", memberId);
        List<Font> fonts = fontRepository.findTop3OrderByDownloadAndBookmarkCountDesc();
        log.debug("Service detail: Found {} popular fonts globally", fonts.size());

        List<FontResponse> result;
        if (memberId == null) {
            result = fonts.stream()
                    .map(font -> FontResponse.from(font, false))
                    .collect(Collectors.toList());
        } else {
            Member member = memberService.getOrThrowById(memberId);
            result = fonts.stream()
                    .map(f -> {
                        boolean bookmarked = bookmarkRepository.existsByMemberIdAndFontId(member.getId(), f.getId());
                        return FontResponse.from(f, bookmarked);
                    })
                    .collect(Collectors.toList());
        }
        
        log.info("Service completed: Retrieved {} globally popular fonts", result.size());
        return result;
    }

    @Override
    @Transactional
    public Font updateProgress(Long fontId, FontProgressUpdateDTO fontProgressUpdateDTO) {
        log.info("Service executing: Updating font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);

        Font updatedFont = fontRepository.save(targetFont.updateProgress(fontProgressUpdateDTO));
        log.info("Service completed: Font ID: {} updated successfully", fontId);
        return updatedFont;
    }

    @Override
    @Transactional
    public FontResponse fontDownload(Long memberId, Long fontId) {
        log.info("Service executing: Download font ID: {}", fontId);
        Font targetFont = getOrThrowById(fontId);
        targetFont.increaseDownloadCount();

        // TODO : 폰트 다운로드

        boolean isBookmarked = false;
        if (memberId != null) {
            isBookmarked = bookmarkRepository.existsByMemberIdAndFontId(memberId, fontId);
        }

        fontRepository.save(targetFont);

        log.info("Service completed: Font ID: {} download successfully", fontId);

        return FontResponse.from(targetFont, isBookmarked);
    }

    private void checkFontOwnership(Long requestMemberId, Long targetMemberId) {
        log.debug("Service detail: Checking font ownership: requestMemberId={}, targetMemberId={}", 
                requestMemberId, targetMemberId);
                
        if (!requestMemberId.equals(targetMemberId)) {
            log.warn("Service warning: Font ownership mismatch: requestMemberId={}, targetMemberId={}", 
                    requestMemberId, targetMemberId);
            throw new FontOwnerMismatchException();
        }
    }
}
