package org.fontory.fontorybe.member.controller.port;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.domain.Provide;

public interface MemberCreationService {
    Member createDefaultMember(Provide p);
}
