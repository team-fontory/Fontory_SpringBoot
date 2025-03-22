package org.fontory.fontorybe.font.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
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

@Service
@RequiredArgsConstructor
public class FontServiceImpl implements FontService {
    private final FontRepository fontRepository;
    private final MemberService memberService;

    @Override
    @Transactional
    public Font create(Long memberId, FontCreateDTO fontCreateDTO) {
        Member member = memberService.getOrThrowById(memberId);

        return fontRepository.save(Font.from(fontCreateDTO, member.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontProgressResponse> getFontProgress(Long memberId) {
        List<Font> fonts = fontRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId);

        return fonts.stream()
                .map(FontProgressResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Font update(Long memberId, Long fontId, FontUpdateDTO fontUpdateDTO) {
        Member member = memberService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontOwnership(member.getId(), targetFont.getMemberId());

        return fontRepository.save(targetFont.update(fontUpdateDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public Font getOrThrowById(Long id) {
        return fontRepository.findById(id).orElseThrow(FontNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FontResponse> getFonts(Long memberId, int page, int size) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Font> fontPage = fontRepository.findAllByMemberId(memberId, pageRequest);

        return fontPage.map(FontResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public FontDetailResponse getFont(Long fontId) {
        Font targetFont = getOrThrowById(fontId);
        Member member = memberService.getOrThrowById(targetFont.getMemberId());

        return FontDetailResponse.from(targetFont, member.getNickname());
    }

    @Override
    @Transactional
    public FontDeleteResponse delete(Long memberId, Long fontId) {
        Member member = memberService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontOwnership(member.getId(), targetFont.getMemberId());

        fontRepository.deleteById(targetFont.getId());

        return FontDeleteResponse.from(fontId);
    }

    @Override
    public Page<FontPageResponse> getFontPage(int page, int size, String sortBy, String keyword) {
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
            fontPage = fontRepository.findByNameContaining(keyword, pageRequest);
        }

        return fontPage.map(font -> {
            Member member = memberService.getOrThrowById(font.getMemberId());
            return FontPageResponse.from(font, member.getNickname());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getOtherFonts(Long fontId) {
        Font font = getOrThrowById(fontId);
        Member member = memberService.getOrThrowById(font.getMemberId());

        List<Font> fonts = fontRepository.findTop3ByMemberIdAndIdNotOrderByCreatedAtDesc(member.getId(), fontId);

        return fonts.stream()
                .map(FontResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getMyPopularFonts(Long memberId) {
        Member member = memberService.getOrThrowById(memberId);

        List<Font> fonts = fontRepository.findTop4ByMemberIdOrderByDownloadAndBookmarkCountDesc(memberId);

        return fonts.stream()
                .map(FontResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FontResponse> getPopularFonts() {
        List<Font> fonts = fontRepository.findTop3OrderByDownloadAndBookmarkCountDesc();

        return fonts.stream()
                .map(FontResponse::from)
                .collect(Collectors.toList());
    }

    private void checkFontOwnership(Long requestMemberId, Long targetMemberId) {
        if (!requestMemberId.equals(targetMemberId)) {
            throw new FontOwnerMismatchException();
        }
    }
}
