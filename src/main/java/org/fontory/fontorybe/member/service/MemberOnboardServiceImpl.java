package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberAlreadyJoinedException;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 온보딩 관련 비즈니스 로직을 처리하는 서비스 구현체
 * OAuth2 로그인 후 신규 회원의 초기 정보 설정 및 기존 회원 조회를 담당
 */
@Slf4j
@Builder
@Service
@RequiredArgsConstructor
public class MemberOnboardServiceImpl implements MemberOnboardService {
    private final MemberRepository memberRepository;
    private final MemberLookupService memberLookupService;
    private final MemberCreationService memberCreationService;
    private final FileService fileService;
    private final MemberValidationService memberValidationService;

    /**
     * OAuth2 로그인 시 기존 회원을 조회하거나 신규 회원을 생성
     * Provide 엔티티의 memberId 존재 여부로 신규/기존 회원을 구분
     * 
     * @param p OAuth2 인증 정보를 담은 Provide 엔티티
     * @return 조회되거나 생성된 회원 정보
     */
    @Override
    @Transactional
    public Member fetchOrCreateMember(Provide p) {
        log.info("Fetching or creating member for OAuth2 provide: provideId={}, memberId={}",
                p.getId(), p.getMemberId());

        if (p.getMemberId()==null) {
            // 신규 회원: 기본 정보로 회원 생성
            log.info("No existing member found, creating new member: provideId={}", p.getId());
            Member newMember = memberCreationService.createDefaultMember(p);
            log.info("New member created successfully: memberId={}, provideId={}",
                    newMember.getId(), p.getId());
            return newMember;
        } else {
            // 기존 회원: ID로 조회
            log.info("Existing member found, fetching member: memberId={}, provideId={}",
                    p.getMemberId(), p.getId());
            return memberLookupService.getOrThrowById(p.getMemberId());
        }
    }

    /**
     * 신규 회원의 초기 정보를 설정 (온보딩 프로세스)
     * 닉네임, 성별, 출생년도 등 추가 정보를 입력받아 회원 상태를 활성화
     * 
     * @param requestMemberId 정보를 초기화할 회원 ID
     * @param initNewMemberInfoRequest 초기화할 회원 정보
     * @return 초기화된 회원 정보
     * @throws MemberAlreadyJoinedException 이미 활성화된 회원인 경우
     * @throws MemberContainsBadWordException 금지 단어가 포함된 경우
     * @throws MemberDuplicateNameExistsException 중복된 닉네임인 경우
     */
    @Override
    @Transactional
    public Member initNewMemberInfo(Long requestMemberId,
                                    InitMemberInfoRequest initNewMemberInfoRequest) {
        log.info("Initializing new member info: memberId={}, nickname={}",
                requestMemberId, initNewMemberInfoRequest.getNickname());

        Member targetMember = memberLookupService.getOrThrowById(requestMemberId);

        if (targetMember.getStatus() == MemberStatus.ACTIVATE) {
            log.warn("Member already activated, cannot reinitialize: memberId={}, status={}",
                    requestMemberId, targetMember.getStatus());
            throw new MemberAlreadyJoinedException();
        }

        // 닉네임 유효성 검사 (금지 단어 + 중복)
        log.debug("Validating nickname for member: memberId={}, nickname={}",
                requestMemberId, initNewMemberInfoRequest.getNickname());
        memberValidationService.validateNickname(initNewMemberInfoRequest.getNickname());

        Member updated = memberRepository.save(targetMember.initNewMemberInfo(initNewMemberInfoRequest));
        log.info("Member info initialized successfully: memberId={}, nickname={}, status={}",
                updated.getId(), updated.getNickname(), updated.getStatus());
        return updated;
    }
}
