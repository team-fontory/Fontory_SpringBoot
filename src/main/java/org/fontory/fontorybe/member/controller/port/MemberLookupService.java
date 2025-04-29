package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;

public interface MemberLookupService {
    Member getOrThrowById(Long id);
    boolean existsByNickname(String nick);
}
