package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;

public interface MemberUpdateService {
    Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest);
    Member disable(Long requestMemberId);
}
