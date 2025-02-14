package org.fontory.fontorybe.font.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FontServiceImpl implements FontService {
    private final FontRepository fontRepository;
    private final MemberService memberService;

    @Override
    @Transactional
    public Font create(Long requestMemberId, FontCreateDTO fontCreateDTO) {
        Member targetMember = memberService.getOrThrowById(requestMemberId);

        return fontRepository.save(Font.from(fontCreateDTO, targetMember.getId()));
    }
}
