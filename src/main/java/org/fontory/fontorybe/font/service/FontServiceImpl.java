package org.fontory.fontorybe.font.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
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

        return fontPage.map(font -> FontResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .example(font.getExample())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public FontDetailResponse getFont(Long memberId, Long fontId) {
        Member member = memberService.getOrThrowById(memberId);
        Font targetFont = getOrThrowById(fontId);

        checkFontOwnership(member.getId(), targetFont.getMemberId());

        return FontDetailResponse.from(targetFont);
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

    private void checkFontOwnership(Long requestMemberId, Long targetMemberId) {
        if (!requestMemberId.equals(targetMemberId)) {
            throw new FontOwnerMismatchException();
        }
    }
}
