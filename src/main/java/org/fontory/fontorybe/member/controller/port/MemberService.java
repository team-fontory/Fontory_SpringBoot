package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.transaction.annotation.Transactional;

public interface MemberService {
    Member getOrThrowById(Long id);
    Member create(MemberCreate memberCreate, Long provideId);
    Member update(Long requestMemberId, Long memberId, MemberUpdate memberUpdate);
    Boolean isDuplicateNameExists(String targetName);
    Member disable(Long requestMemberId, Long memberId);
}
