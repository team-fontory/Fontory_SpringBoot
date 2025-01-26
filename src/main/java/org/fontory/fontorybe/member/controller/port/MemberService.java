package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.provide.domain.Provide;

public interface MemberService {
    Member getById(Long id);
    Member create(MemberCreateDto memberCreateDto, Provide provide);
}
