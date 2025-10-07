package org.fontory.fontorybe.unit.mock;

import com.vane.badwordfiltering.BadWordFiltering;
import org.fontory.fontorybe.authentication.adapter.outbound.CookieUtilsImpl;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.bookmark.service.BookmarkServiceImpl;
import org.fontory.fontorybe.bookmark.service.port.BookmarkRepository;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProviderImpl;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.adapter.outbound.RedisTokenStorage;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.file.adapter.inbound.FileRequestMapper;
import org.fontory.fontorybe.file.application.FileServiceImpl;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.member.controller.MemberController;
import org.fontory.fontorybe.member.controller.ProfileController;
import org.fontory.fontorybe.member.controller.RegistrationController;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberCreationService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.controller.port.MemberUpdateService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.MemberDefaults;
import org.fontory.fontorybe.member.service.MemberCreationServiceImpl;
import org.fontory.fontorybe.member.service.MemberLookupServiceImpl;
import org.fontory.fontorybe.member.service.MemberOnboardServiceImpl;
import org.fontory.fontorybe.member.service.MemberUpdateServiceImpl;
import org.fontory.fontorybe.member.service.port.MemberRepository;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.service.ProvideServiceImpl;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

import static org.fontory.fontorybe.TestConstants.*;

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
    public final BookmarkRepository bookmarkRepository;
    public final FontRepository fontRepository;

    public final MemberLookupService memberLookupService;
    public final MemberCreationService memberCreationService;
    public final MemberOnboardService memberOnboardService;
    public final CloudStorageService cloudStorageService;
    public final ProvideService provideService;
    public final MemberUpdateService memberUpdateService;
    public final TokenStorage tokenStorage;
    public final AuthService authService;
    public final FileService fileService;
    public final FontService fontService;
    public final BookmarkService bookmarkService;

    public final ProfileController profileController;
    public final MemberController memberController;
    public final RegistrationController registrationController;

    public final ApplicationEventPublisher eventPublisher;
    public final FileRequestMapper fileRequestMapper;
    public final FakeRedisTemplate fakeRedisTemplate;
    public final JwtTokenProvider jwtTokenProvider;
    public final MemberDefaults memberDefaults;
    public final CookieUtils cookieUtils;
    public final S3Config s3Config;
    public final BadWordFiltering badWordFiltering;

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
        bookmarkRepository = new FakeBookmarkRepository();
        fontRepository = new FakeFontRepository();

        tokenStorage = new RedisTokenStorage(fakeRedisTemplate, props);

        badWordFiltering = new BadWordFiltering();

        s3Config = new S3Config(
                TEST_AWS_REGION,
                TEST_CDN_URL,
                TEST_PROFILE_BUCKET_NAME,
                TEST_TEMPLATE_BUCKET_NAME,
                TEST_FONT_BUCKET_NAME,
                TEST_PROFILE_PREFIX,
                TEST_TEMPLATE_PREFIX,
                TEST_FONT_PREFIX);

        cloudStorageService = new FakeCloudStorageService(s3Config);

        provideService = ProvideServiceImpl.builder()
                .provideRepository(provideRepository)
                .build();

        memberLookupService = MemberLookupServiceImpl.builder()
                .memberRepository(memberRepository)
                .build();

        memberUpdateService = MemberUpdateServiceImpl.builder()
                .memberLookupService(memberLookupService)
                .memberRepository(memberRepository)
                .provideService(provideService)
                .jwtTokenProvider(jwtTokenProvider)
                .badWordFiltering(badWordFiltering)
                .build();

        memberDefaults = new MemberDefaults(
                LocalDate.of(1999, 12, 31));

        fileService = FileServiceImpl.builder()
                .memberLookupService(memberLookupService)
                .memberDefaults(memberDefaults)
                .fileRepository(fileRepository)
                .fileRequestMapper(fileRequestMapper)
                .eventPublisher(eventPublisher)
                .cloudStorageService(cloudStorageService)
                .build();

        fontService = new FakeFontService(fontRepository);

        memberCreationService = MemberCreationServiceImpl.builder()
                .memberDefaults(memberDefaults)
                .memberRepository(memberRepository)
                .provideService(provideService)
                .build();

        authService = AuthService.builder()
                .memberLookupService(memberLookupService)
                .cookieUtils(cookieUtils)
                .tokenStorage(tokenStorage)
                .jwtTokenProvider(jwtTokenProvider)
                .build();

        memberOnboardService = MemberOnboardServiceImpl
                .builder()
                .fileService(fileService)
                .memberRepository(memberRepository)
                .memberLookupService(memberLookupService)
                .memberCreationService(memberCreationService)
                .badWordFiltering(badWordFiltering)
                .build();

        bookmarkService = new BookmarkServiceImpl(
                bookmarkRepository,
                fontRepository,
                memberLookupService,
                fontService,
                cloudStorageService
        );

        memberController = MemberController.builder()
                .memberLookupService(memberLookupService)
                .cloudStorageService(cloudStorageService)
                .build();


        profileController = ProfileController.builder()
                .authService(authService)
                .fileService(fileService)
                .memberLookupService(memberLookupService)
                .memberUpdateService(memberUpdateService)
                .cloudStorageService(cloudStorageService)
                .build();

        registrationController = RegistrationController.builder()
                .memberLookupService(memberLookupService)
                .memberOnboardService(memberOnboardService)
                .cloudStorageService(cloudStorageService)
                .fileService(fileService)
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

    public Member create(InitMemberInfoRequest initNewMemberInfoRequest, Provide provide) {
        Member defaultMember = memberCreationService.createDefaultMember(provide);
        MockMultipartFile file = new MockMultipartFile(
                TEST_FILE_NAME,              // RequestPart 이름
                TEST_FILE_NAME,          // 원본 파일명
                "image/png",         // Content-Type
                "dummy-image-data".getBytes()  // 파일 내용
        );
        return memberOnboardService.initNewMemberInfo(defaultMember.getId(), initNewMemberInfoRequest);
    }

    public final ProvideCreateDto testMemberProvideCreateDto = new ProvideCreateDto(TEST_MEMBER_PROVIDER, TEST_MEMBER_PROVIDED_ID, TEST_MEMBER_EMAIL);
    public Provide testMemberProvide;
    public Provide newMemberProvide;

    public final InitMemberInfoRequest newInitMemberInfoRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
    public final ProvideCreateDto newMemberProvideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);

    public Member createNotInitedMember() {
        Provide newMemberProvide = provideService.create(newMemberProvideCreateDto);
        return memberOnboardService.fetchOrCreateMember(newMemberProvide);
    }

    public Member createTestMember() {
        testMemberProvide = provideService.create(testMemberProvideCreateDto);
        InitMemberInfoRequest initMemberInfoRequest = new InitMemberInfoRequest(TEST_MEMBER_NICKNAME, TEST_MEMBER_GENDER, TEST_MEMBER_BIRTH);
        return create(initMemberInfoRequest, testMemberProvide);
    }

    public Member createNewMember() {
        newMemberProvide = provideService.create(newMemberProvideCreateDto);
        InitMemberInfoRequest initNewMemberInfoRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH);
        return create(initNewMemberInfoRequest, newMemberProvide);
    }
}
