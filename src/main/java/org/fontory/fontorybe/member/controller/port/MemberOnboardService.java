package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.domain.Provide;

public interface MemberOnboardService {
    Member createDefaultMember(Provide p);
    Member fetchOrCreateMember(Provide p);
    Member initNewMemberInfo(Long requestMemberId, MemberCreateRequest memberCreateRequest);
}
