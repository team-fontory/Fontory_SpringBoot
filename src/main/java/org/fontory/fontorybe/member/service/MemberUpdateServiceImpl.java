package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyDisabledException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 회원 정보 수정 및 비활성화 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 회원 프로필 업데이트 및 탈퇴 처리 기능을 제공
 */
@Service
@Builder
@RequiredArgsConstructor
public class MemberUpdateServiceImpl implements MemberUpdateService {
    private final MemberLookupService memberLookupService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ProvideService provideService;
    private final MemberValidationService memberValidationService;

    /**
     * 회원 프로필 정보를 업데이트
     * 닉네임 변경 시 금지 단어 및 중복 여부를 검사
     * 
     * @param requestMemberId 수정할 회원의 ID
     * @param memberUpdateRequest 수정할 회원 정보
     * @return 업데이트된 회원 정보
     * @throws MemberContainsBadWordException 금지 단어가 포함된 경우
     * @throws MemberDuplicateNameExistsException 중복된 닉네임인 경우
     */
    @Override
    @Transactional
    public Member update(Long requestMemberId, MemberUpdateRequest memberUpdateRequest) {
        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);
        
        // 닉네임 변경 시 유효성 검사
        String newNickname = memberUpdateRequest.getNickname();
        memberValidationService.validateNoBadWords(newNickname);
        memberValidationService.validateNicknameChangeNotDuplicated(targetMember.getNickname(), newNickname);

        return memberRepository.save(targetMember.update(memberUpdateRequest));
    }

    /**
     * 회원 계정을 비활성화 (소프트 삭제)
     * 이미 비활성화된 회원인 경우 예외 발생
     * 
     * @param requestMemberId 비활성화할 회원의 ID
     * @return 비활성화된 회원 정보
     * @throws MemberAlreadyDisabledException 이미 비활성화된 회원인 경우
     */
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
}
