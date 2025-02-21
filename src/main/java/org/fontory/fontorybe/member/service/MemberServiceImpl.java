package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyExistException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Builder
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ProvideService provideService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public Member getOrThrowById(Long id) {
        return Optional.ofNullable(id)
                .flatMap(memberRepository::findById)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isDuplicateNameExists(String targetName) {
        return memberRepository.existsByNickname(targetName);
    }

    @Override
    @Transactional
    public Member create(MemberCreateRequest memberCreateRequest) {
        Long provideId = jwtTokenProvider.getProvideId(memberCreateRequest.getProvideToken());
        Provide provide = provideService.getOrThrownById(provideId);

        // 닉네임 중복확인
        if (isDuplicateNameExists(memberCreateRequest.getNickname())) {
             throw new MemberDuplicateNameExistsException();
        } else if (provide.getMemberId() != null) {
            throw new MemberAlreadyExistException();
        }

        Member createdMember = memberRepository.save(Member.from(memberCreateRequest, provide));
        provideService.setMember(provide, createdMember);

        return createdMember;
    }

    @Override
    @Transactional
    public Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest) {
        Member targetMember = getOrThrowById(requestMemberId);

        if (!targetMember.getNickname().equals(memberUpdateRequest.getNickname()) &&
                isDuplicateNameExists(memberUpdateRequest.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        }

        return memberRepository.save(targetMember.update(memberUpdateRequest));
    }

    @Override
    @Transactional
    public Member disable(Long requestMemberId) {
        Member targetMember = getOrThrowById(requestMemberId);

        if (targetMember.getDeletedAt() != null) {
            throw new MemberAlreadyDisabledException();
        }

        targetMember.disable();
        return memberRepository.save(targetMember);
    }
}
