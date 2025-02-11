package org.fontory.fontorybe.member.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.domain.exception.MemberDuplicateNameExistsException;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.domain.exception.MemberOwnerMismatchException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ProvideService provideService;

    @Override
    @Transactional(readOnly = true)
    public Member getOrThrowById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isDuplicateNameExists(String targetName) {
        return memberRepository.existsByNickname(targetName);
    }

    @Override
    @Transactional
    public Member create(MemberCreate memberCreate, Long provideId) {
        // 닉네임 중복확인
        if (isDuplicateNameExists(memberCreate.getNickname())) {
             throw new MemberDuplicateNameExistsException();
        }

        Provide provide = provideService.getOrThrownById(provideId);
        return memberRepository.save(Member.from(memberCreate, provide));
    }

    @Override
    @Transactional
    public Member update(Long requestMemberId, Long memberId, MemberUpdate memberUpdate) {
        Member targetMember = getOrThrowById(memberId);
        checkMemberOwnership(requestMemberId, memberId);

        if (!targetMember.getNickname().equals(memberUpdate.getNickname()) &&
                isDuplicateNameExists(memberUpdate.getNickname())) {
            throw new MemberDuplicateNameExistsException();
        }

        return memberRepository.save(targetMember.update(memberUpdate));
    }

    @Override
    @Transactional
    public Member disable(Long requestMemberId, Long memberId) {
        Member targetMember = getOrThrowById(memberId);
        checkMemberOwnership(requestMemberId, memberId);

        if (targetMember.getDeletedAt() != null) {
            throw new MemberAlreadyDisabledException();
        }

        targetMember.disable();
        return memberRepository.save(targetMember);
    }

    private void checkMemberOwnership(Long requestMemberId, Long targetMemberId) {
        if (!requestMemberId.equals(targetMemberId)) {
            throw new MemberOwnerMismatchException();
        }
    }
}
