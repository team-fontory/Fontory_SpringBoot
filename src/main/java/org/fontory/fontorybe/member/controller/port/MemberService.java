package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.provide.domain.Provide;

public interface MemberService {
    Member getOrThrowById(Long id);
    Member create(MemberCreateRequest memberCreateRequest, Provide provide);
    Member initNewMemberInfo(Long requestMemberId, MemberCreateRequest memberCreateRequest);
    Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest);
    boolean isDuplicateNameExists(String targetName);
    Member setProfileImageKey(Member requetMember, String profileImageKey);
    Member disable(Long requestMemberId);
}
