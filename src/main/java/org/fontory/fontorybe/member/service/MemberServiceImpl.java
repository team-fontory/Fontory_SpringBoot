package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
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
    public boolean isDuplicateNameExists(String targetName) {
        return memberRepository.existsByNickname(targetName);
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
    public Member setProfileImageKey(Member requetMember, String profileImageKey) {
        return memberRepository.save(requetMember.setProfileImageKey(profileImageKey));
    }

    @Override
    @Transactional
    public Member disable(Long requestMemberId) {
        Member targetMember = getOrThrowById(requestMemberId);

        if (targetMember.getDeletedAt() != null) {
            throw new MemberAlreadyDisabledException();
        }
        System.out.println("targetMember = " + targetMember);
        targetMember.disable();
        System.out.println("targetMember = " + targetMember);
        Member save = memberRepository.save(targetMember);
        System.out.println("save = " + save);
        return memberRepository.save(targetMember);
    }
}
