package org.fontory.fontorybe.member.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberOnboardServiceImpl implements MemberOnboardService {
    private final MemberDefaults defaults;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ProvideService provideService;

    @Override
    @Transactional
    public Member createDefaultMember(Provide p) {
        if (p.getMemberId() != null) { throw new MemberAlreadyExistException(); }
        MemberCreateRequest req = MemberCreateRequest.builder()
                .nickname(UUID.randomUUID().toString())
                .gender(defaults.getGender())
                .birth(defaults.getBirth())
                .terms(defaults.getTerms())
                .profileImageKey(defaults.getProfileImageKey())
                .build();
        Member defaultMember = memberRepository.save(Member.from(req, p));
        provideService.setMember(p, defaultMember);

        return defaultMember;
    }

    @Override
    @Transactional
    public Member fetchOrCreateMember(Provide p) {
        if (p.getMemberId()==null) {
            return createDefaultMember(p);
        } else {
            return memberService.getOrThrowById(p.getMemberId());
        }
    }

    @Override
    @Transactional
    public Member initNewMemberInfo(Long requestMemberId, MemberCreateRequest memberCreateRequest) {
        Member targetMember = memberService.getOrThrowById(requestMemberId);
        if (memberService.isDuplicateNameExists(memberCreateRequest.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        }

        return memberRepository.save(targetMember.initNewMemberInfo(memberCreateRequest));
    }
}
