package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.authentication.adapter.outbound.CookieUtilsImpl;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProviderImpl;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.adapter.outbound.RedisTokenStorage;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.service.MemberServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class TestContainer {
    public final JwtProperties props;

    public final String secretKeyForTest = generateSecretKey();
    public final String secretKeyForTest2 = generateSecretKey();
    public final String secretKeyForTest3 = generateSecretKey();
    public final String secretKeyForTest4 = generateSecretKey();
    public final long accessTokenValidityMs = 900000;
    public final long refreshTokenValidityMs = 604800000;
    public final long tempTokenValidityMs = 900000;

    public final MemberRepository memberRepository;
    public final ProvideRepository provideRepository;

    public final ProvideService provideService;
    public final MemberService memberService;

    public final FakeRedisTemplate fakeRedisTemplate;

    public final TokenStorage tokenStorage;
    public final AuthService authService;

    public final JwtTokenProvider jwtTokenProvider;
    public final CookieUtils cookieUtils;

    public TestContainer() {
        fakeRedisTemplate = new FakeRedisTemplate();
        props = new JwtProperties(
                secretKeyForTest,
                secretKeyForTest2,
                secretKeyForTest3,
                secretKeyForTest4,
                accessTokenValidityMs,
                refreshTokenValidityMs,
                tempTokenValidityMs);

        jwtTokenProvider = new JwtTokenProviderImpl(props);
        cookieUtils = new CookieUtilsImpl(props);

        memberRepository = new FakeMemberRepository();
        provideRepository = new FakeProvideRepository();

        tokenStorage = new RedisTokenStorage(jwtTokenProvider, fakeRedisTemplate, props);

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberService = MemberServiceImpl.builder()
                .memberRepository(memberRepository)
                .provideService(provideService)
                .jwtTokenProvider(jwtTokenProvider)
                .build();

        authService = new AuthService(cookieUtils, tokenStorage, jwtTokenProvider, memberService);
    }

    private static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);
            SecretKey secretKey = keyGenerator.generateKey();
            return bytesToHex(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
