package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Builder
@Service
@RequiredArgsConstructor
public class MemberCreationServiceImpl implements MemberCreationService {
    private final MemberRepository memberRepository;
    private final ProvideService provideService;
    private final MemberDefaults memberDefaults;

    @Override
    @Transactional
    public Member createDefaultMember(Provide p) {
        if (p.getMemberId() != null) { throw new MemberAlreadyExistException(); }
        String newNickname = UUID.randomUUID().toString();
        Member defaultMember = memberRepository.save(Member.fromDefaults(memberDefaults, newNickname, p));
        provideService.setMember(p, defaultMember);

        return defaultMember;
    }
}
