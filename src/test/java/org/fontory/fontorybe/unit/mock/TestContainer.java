package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.TokenService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.service.MemberServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.data.redis.core.RedisTemplate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

import static org.mockito.Mockito.mock;

public class TestContainer {
    public final String secretKeyForTest = generateSecretKey();
    public final String secretKeyForTest2 = generateSecretKey();
    public final MemberRepository memberRepository;
    public final ProvideRepository provideRepository;

    public final ProvideService provideService;
    public final MemberService memberService;

    public final FakeRedisTemplate fakeRedisTemplate;

    public final TokenService tokenService;
    public final AuthService authService;

    public final JwtTokenProvider jwtTokenProvider;

    public TestContainer() {
        fakeRedisTemplate = new FakeRedisTemplate();
        jwtTokenProvider = new JwtTokenProvider(secretKeyForTest, secretKeyForTest2);

        memberRepository = new FakeMemberRepository();
        provideRepository = new FakeProvideRepository();

        tokenService = new TokenService(jwtTokenProvider, fakeRedisTemplate);
        authService = new AuthService(tokenService, jwtTokenProvider);

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberService = MemberServiceImpl.builder()
                .memberRepository(memberRepository)
                .provideService(provideService)
                .jwtTokenProvider(jwtTokenProvider)
                .build();
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
