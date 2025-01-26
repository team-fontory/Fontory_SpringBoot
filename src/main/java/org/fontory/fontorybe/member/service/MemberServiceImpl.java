package org.fontory.fontorybe.member.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Override
    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Member create(MemberCreateDto memberCreateDto, Provide provide) {
        Member member = Member.from(memberCreateDto, provide);
        return memberRepository.save(member);
    }
}
