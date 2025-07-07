package org.fontory.fontorybe.member.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberContainsBadWordException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Builder
@RequiredArgsConstructor
public class MemberUpdateServiceImpl implements MemberUpdateService {
    private final MemberLookupService memberLookupService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ProvideService provideService;
    private final BadWordFiltering badWordFiltering;

    @Override
    @Transactional
    public Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);

        if (!targetMember.getNickname().equals(memberUpdateRequest.getNickname()) &&
                memberLookupService.existsByNickname(memberUpdateRequest.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        }

        checkContainsBadWord(memberUpdateRequest.getNickname());

        return memberRepository.save(targetMember.update(memberUpdateRequest));
    }

    @Override
    @Transactional
    public Member setProfileImageKey(Long requestMemberId, String profileImageKey) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);
        Member member = targetMember.setProfileImageKey(profileImageKey);
        return memberRepository.save(member);
    }

    @Override
    @Transactional
    public Member disable(Long requestMemberId) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);

        if (targetMember.getDeletedAt() != null) {
            throw new MemberAlreadyDisabledException();
        }
        targetMember.disable();

        return memberRepository.save(targetMember);
    }

    private void checkContainsBadWord(String nickname) {
        if (badWordFiltering.blankCheck(nickname)) {
            throw new MemberContainsBadWordException();
        }
    }
}
