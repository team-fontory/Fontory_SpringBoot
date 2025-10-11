package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;

import java.util.Optional;

public interface MemberLookupService {
    Member getOrThrowById(Long id);
    boolean existsByNickname(String nick);
    Optional<Member> findById(Long id);
}
