package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 회원 생성 관련 비즈니스 로직을 처리하는 서비스 구현체
 * OAuth2 인증 후 기본 회원 정보를 생성하는 역할을 담당
 */
@Slf4j
@Builder
@Service
@RequiredArgsConstructor
public class MemberCreationServiceImpl implements MemberCreationService {
    private final MemberRepository memberRepository;
    private final ProvideService provideService;
    private final MemberDefaults memberDefaults;

    /**
     * OAuth2 인증 정보를 기반으로 기본 회원을 생성
     * UUID 기반의 임시 닉네임을 생성하고, 기본 프로필 이미지와 소개글을 설정
     * 
     * @param p OAuth2 인증 정보를 담고 있는 Provide 엔티티
     * @return 생성된 기본 회원 정보
     * @throws MemberAlreadyExistException 이미 회원이 존재하는 경우
     */
    @Override
    @Transactional
    public Member createDefaultMember(Provide p) {
        log.info("Creating default member for OAuth2 provider: provideId={}, provider={}, email={}",
                p.getId(), p.getProvider(), p.getEmail());

        // 이미 회원이 연결되어 있는 경우 예외 처리
        if (p.getMemberId() != null) {
            log.warn("Member already exists for provide: provideId={}, existingMemberId={}",
                    p.getId(), p.getMemberId());
            throw new MemberAlreadyExistException();
        }

        // UUID를 사용한 임시 닉네임 생성
        String newNickname = UUID.randomUUID().toString();
        log.debug("Generated temporary nickname for new member: nickname={}", newNickname);

        // 기본값을 사용하여 회원 생성 및 저장
        Member defaultMember = memberRepository.save(Member.fromDefaults(memberDefaults, newNickname, p));
        log.info("Default member created and saved: memberId={}, provideId={}",
                defaultMember.getId(), p.getId());

        // Provide와 Member 연결
        provideService.setMember(p, defaultMember);
        log.debug("Provide linked to member: provideId={}, memberId={}", p.getId(), defaultMember.getId());

        return defaultMember;
    }
}
