package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;

public interface MemberService {
    Member getOrThrowById(Long id);
    Member create(MemberCreate memberCreateDto, Long provideId);
    Member update(Long requestMemberId, Long memberId, MemberUpdate memberUpdate);
    Boolean isDuplicateNameExists(String targetName);
}
