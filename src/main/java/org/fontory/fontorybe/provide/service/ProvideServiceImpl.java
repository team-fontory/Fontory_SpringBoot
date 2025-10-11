package org.fontory.fontorybe.provide.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 제공자 정보 관리 서비스 구현체
 * 외부 OAuth2 제공자(Google 등)로부터 받은 사용자 정보를 관리
 */
@Slf4j
@Service
@Builder
@RequiredArgsConstructor
public class ProvideServiceImpl implements ProvideService {
    private final ProvideRepository provideRepository;

    /**
     * ID로 Provide 엔티티를 조회
     * 
     * @param id 조회할 Provide ID
     * @return 조회된 Provide 엔티티
     * @throws ProvideNotFoundException 해당 ID의 Provide가 없는 경우
     */
    @Override
    public Provide getOrThrownById(Long id) {
        log.debug("Looking up provide by ID: provideId={}", id);
        return provideRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Provide not found: provideId={}", id);
                    return new ProvideNotFoundException();
                });
    }

    /**
     * Provide DTO를 바탕으로 새로운 Provide 엔티티 생성
     * 
     * @param provideCreateDto 생성할 Provide 정보
     * @return 생성된 Provide 엔티티
     */
    @Override
    @Transactional
    public Provide create(ProvideCreateDto provideCreateDto) {
        log.info("Creating provide from DTO: provider={}, email={}",
                provideCreateDto.getProvider(), provideCreateDto.getEmail());
        Provide provide = Provide.from(provideCreateDto);
        Provide saved = provideRepository.save(provide);
        log.info("Provide created successfully: provideId={}", saved.getId());
        return saved;
    }

    /**
     * OAuth2 제공자 정보를 바탕으로 새로운 Provide 엔티티 생성
     * 
     * @param provider OAuth2 제공자 타입 (Google, Facebook 등)
     * @param email 사용자 이메일
     * @param providedId OAuth2 제공자가 발급한 고유 사용자 ID
     * @return 생성된 Provide 엔티티
     */
    @Override
    @Transactional
    public Provide create(Provider provider, String email, String providedId) {
        log.info("Creating provide: provider={}, email={}, providedId={}",
                provider, email, providedId);
        Provide provide = Provide.from(provider, providedId, email);
        Provide saved = provideRepository.save(provide);
        log.info("Provide created successfully: provideId={}", saved.getId());
        return saved;
    }

    /**
     * Provide와 Member를 연결
     * OAuth2 로그인 후 회원 가입 시 사용
     * 
     * @param provide 연결할 Provide 엔티티
     * @param member 연결할 회원 엔티티
     * @return 업데이트된 Provide 엔티티
     */
    @Override
    @Transactional
    public Provide setMember(Provide provide, Member member) {
        log.info("Linking member to provide: provideId={}, memberId={}", provide.getId(), member.getId());
        provide.setMember(member.getId());
        Provide updated = provideRepository.save(provide);
        log.debug("Member linked successfully: provideId={}, memberId={}", provide.getId(), member.getId());
        return updated;
    }
}
