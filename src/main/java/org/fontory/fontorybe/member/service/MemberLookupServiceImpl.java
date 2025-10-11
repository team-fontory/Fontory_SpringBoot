package org.fontory.fontorybe.member.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 회원 조회 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 회원 정보 조회 및 존재 여부 확인 기능을 제공
 */
@Slf4j
@Builder
@Service
@RequiredArgsConstructor
public class MemberLookupServiceImpl implements MemberLookupService {

    private final MemberRepository memberRepository;

    /**
     * ID로 회원을 조회하고, 존재하지 않으면 예외 발생
     * 
     * @param id 조회할 회원 ID
     * @return 조회된 회원 정보
     * @throws MemberNotFoundException 회원이 존재하지 않거나 ID가 null인 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Member getOrThrowById(Long id) {
        log.debug("Looking up member by ID: memberId={}", id);
        if (id == null) {
            log.warn("Member lookup failed: memberId is null");
            throw new MemberNotFoundException();
        }
        return memberRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Member not found: memberId={}", id);
                    return new MemberNotFoundException();
                });
    }

    /**
     * 특정 닉네임이 이미 사용 중인지 확인
     * 
     * @param targetName 확인할 닉네임
     * @return 사용 중이면 true, 사용 가능하면 false
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNickname(String targetName) {
        log.debug("Checking nickname existence: nickname={}", targetName);
        boolean exists = memberRepository.existsByNickname(targetName);
        log.debug("Nickname existence check result: nickname={}, exists={}", targetName, exists);
        return exists;
    }
}
