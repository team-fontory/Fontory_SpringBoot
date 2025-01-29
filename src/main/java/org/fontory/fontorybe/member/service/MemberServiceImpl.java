package org.fontory.fontorybe.member.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.domain.dto.MemberUpdateDto;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ProvideService provideService;

    @Override
    public Member getOrThrowById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Member create(MemberCreateDto memberCreateDto, Long provideId) {
        Provide provide = provideService.getOrThrownById(provideId);
        Member member = Member.from(memberCreateDto, provide);

        return memberRepository.save(member);
    }

    @Override
    public Member update(Long memberId, MemberUpdateDto memberUpdateDto) {
        Member member = getOrThrowById(memberId);
        return memberRepository.save(member.update(memberUpdateDto));
    }
}
