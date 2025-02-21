package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;

public interface MemberService {
    Member getOrThrowById(Long id);
    Member create(MemberCreateRequest memberCreateRequest, Long provideId);
    Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest);
    Boolean isDuplicateNameExists(String targetName);
    Member disable(Long requestMemberId);
}
