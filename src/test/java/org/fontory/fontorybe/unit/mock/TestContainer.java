package org.fontory.fontorybe.unit.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fontory.fontorybe.authentication.adapter.outbound.CookieUtilsImpl;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProviderImpl;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.adapter.outbound.RedisTokenStorage;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.file.adapter.inbound.FileRequestMapper;
import org.fontory.fontorybe.file.application.FileServiceImpl;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.member.controller.MemberController;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.fontory.fontorybe.member.service.MemberOnboardServiceImpl;
import org.fontory.fontorybe.member.service.MemberServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.context.ApplicationEventPublisher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

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
    public final FileRepository fileRepository;

    public final MemberOnboardService memberOnboardService;
    public final CloudStorageService cloudStorageService;
    public final ProvideService provideService;
    public final MemberService memberService;
    public final TokenStorage tokenStorage;
    public final AuthService authService;
    public final FileService fileService;

    public final MemberController memberController;

    public final ApplicationEventPublisher eventPublisher;
    public final FileRequestMapper fileRequestMapper;
    public final FakeRedisTemplate fakeRedisTemplate;
    public final JwtTokenProvider jwtTokenProvider;
    public final CookieUtils cookieUtils;
    public final MemberDefaults memberDefaults;

    public TestContainer() {
        props = new JwtProperties(
                secretKeyForTest,
                secretKeyForTest2,
                secretKeyForTest3,
                secretKeyForTest4,
                accessTokenValidityMs,
                refreshTokenValidityMs,
                tempTokenValidityMs);

        eventPublisher = new FakeApplicationEventPublisher();
        fileRequestMapper = new FileRequestMapper();
        fakeRedisTemplate = new FakeRedisTemplate();
        jwtTokenProvider = new JwtTokenProviderImpl(props);
        cookieUtils = new CookieUtilsImpl(props);

        memberRepository = new FakeMemberRepository();
        provideRepository = new FakeProvideRepository();
        fileRepository = new FakeFileRepository();

        tokenStorage = new RedisTokenStorage(fakeRedisTemplate, props);

        cloudStorageService = new FakeCloudStorageService();

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberService = MemberServiceImpl.builder()
                .memberRepository(memberRepository)
                .provideService(provideService)
                .jwtTokenProvider(jwtTokenProvider)
                .build();

        fileService = FileServiceImpl.builder()
                .memberService(memberService)
                .fileRepository(fileRepository)
                .fileRequestMapper(fileRequestMapper)
                .eventPublisher(eventPublisher)
                .cloudStorageService(cloudStorageService)
                .build();

        authService = new AuthService(cookieUtils, tokenStorage, jwtTokenProvider, memberService);
        memberDefaults = new MemberDefaults(
                LocalDate.of(1999, 12, 31),
                false,
                "testUrl");
        memberOnboardService = new MemberOnboardServiceImpl(memberDefaults, memberService, memberRepository, provideService);

        memberController = MemberController.builder()
                .memberOnboardService(memberOnboardService)
                .memberService(memberService)
                .provideService(provideService)
                .jwtTokenProvider(jwtTokenProvider)
                .authService(authService)
                .fileService(fileService)
                .objectMapper(new ObjectMapper())
                .build();
    }

    private static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA512");
            keyGenerator.init(512);
            SecretKey secretKey = keyGenerator.generateKey();
            return bytesToHex(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secret fileKey", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public Member create(MemberCreateRequest memberCreateRequest, Provide provide) {
        Member defaultMember = memberOnboardService.createDefaultMember(provide);
        return memberOnboardService.initNewMemberInfo(defaultMember.getId(), memberCreateRequest);
    }
}
