package org.fontory.fontorybe.member.service.port;

import java.util.Optional;
import org.fontory.fontorybe.member.domain.Member;

public interface MemberRepository {
    Optional<Member> findById(Long id);
    Member save(Member member);
}
