package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.domain.dto.MemberUpdateDto;

public interface MemberService {
    Member getOrThrowById(Long id);
    Member create(MemberCreateDto memberCreateDto, Long provideId);
    Member update(Long memberId, MemberUpdateDto memberUpdateDto);
}
