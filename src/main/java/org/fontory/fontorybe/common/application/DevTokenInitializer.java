package org.fontory.fontorybe.common.application;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.config.jwt.JwtProperties;
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

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevTokenInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final JwtProperties props;
    private final ProvideRepository provideRepository;
    private final MemberRepository memberRepository;
    private final JwtProperties jwtProperties;

    // 고정된 발행 및 만료 시간
    private final Date issuedAt = new Date(1735689600000L);     // 2025-01-01T00:00:00Z
    private final Date expiration = new Date(1767225600000L);     // 2025-12-31T23:59:59Z

    @Getter
    private String fixedTokenForFontCreateServer;

    private SecretKey getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 재시작 컨텍스트인지 확인
        if (event.getApplicationContext().getParent() != null) {
            log.info("Skipping token init in restarted context");
            return;
        }

        // 프록시 빈을 통해 @Transactional이 적용된 메서드 실행
        DevTokenInitializer initializer = event.getApplicationContext().getBean(DevTokenInitializer.class);
        initializer.initTokens();
    }

    @Transactional
    public void initTokens() {
        SecretKey accessSecretKey = getSigningKey(props.getAccessSecretKey());
        SecretKey provideSecretKey = getSigningKey(props.getProvideSecretKey());
        SecretKey fontCreateSecretKey = getSigningKey(props.getFontCreateServerSecretKey());

        // 테스트용 Provide와 Member 생성
        Provide provide = Provide.builder()
                .providedId(UUID.randomUUID().toString())
                .email(UUID.randomUUID().toString())
                .provider(Provider.GOOGLE)
                .build();

        Provide savedProvide = provideRepository.save(provide);

        Member member = Member.builder()
                .gender(Gender.MALE)
                .provideId(savedProvide.getId())
                .terms(true)
                .birth(LocalDate.now())
                .nickname("Tester")
                .profileImageKey(S3Config.getDefaultProfileImageUrl())
                .build();

        Member savedMember = memberRepository.save(member);

        String fixedTokenForProvide = Jwts.builder()
                .setSubject(String.valueOf(savedProvide.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(provideSecretKey)
                .compact();

        String fixedTokenForAuthentication = Jwts.builder()
                .setSubject(String.valueOf(savedMember.getId()))
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(accessSecretKey)
                .compact();

        fixedTokenForFontCreateServer = Jwts.builder()
                .setSubject(jwtProperties.getFontCreateServerSubject())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(fontCreateSecretKey)
                .compact();

        log.info("Provide JWT for development: {}", fixedTokenForProvide);
        log.info("Authentication JWT for development: {}", fixedTokenForAuthentication);
        log.info("FontCreateServer JWT: {}", fixedTokenForFontCreateServer);
    }
}