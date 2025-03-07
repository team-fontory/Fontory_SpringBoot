package org.fontory.fontorybe.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevTokenInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final TokenService tokenService;
    private final ProvideRepository provideRepository;
    private final MemberRepository memberRepository;

    @Value("${jwt.secretKey}")
    private String secretKeyForAuthentication;

    @Value("${jwt.provide.secretKey}")
    private String secretKeyForProvide;

    // 고정된 발행 및 만료 시간
    private final Date issuedAt = new Date(1735689600000L);     // 2025-01-01T00:00:00Z
    private final Date expiration = new Date(1767225600000L);     // 2025-12-31T23:59:59Z

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 컨텍스트가 완전히 초기화된 후, 프록시된 빈을 얻어 @Transactional이 적용된 initTokens()를 호출합니다.
        DevTokenInitializer initializer = event.getApplicationContext().getBean(DevTokenInitializer.class);
        initializer.initTokens();
    }

    @Transactional
    public void initTokens() {
        // 테스트용 Provide와 Member 생성
        Provide provide = Provide.builder()
                .providedId(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString())
                .provider(Provider.GOOGLE)
                .build();

        Member member = Member.builder()
                .gender(Gender.MALE)
                .provideId(provide.getId())
                .terms(true)
                .birth(LocalDate.now())
                .nickname("Tester")
                .profileImage("ProfileImage-URL")
                .build();

        Provide savedProvide = provideRepository.save(provide);
        memberRepository.save(member);

        String fixedTokenForProvide = Jwts.builder()
                .setSubject(String.valueOf(savedProvide.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, secretKeyForProvide)
                .compact();

        String fixedTokenForAuthentication = Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, secretKeyForAuthentication)
                .compact();

        log.info("Provide JWT for development: {}", fixedTokenForProvide);
        log.info("Authentication JWT for development: {}", fixedTokenForAuthentication);
    }
}