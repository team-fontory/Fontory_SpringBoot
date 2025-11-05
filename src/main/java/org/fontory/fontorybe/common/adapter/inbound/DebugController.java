package org.fontory.fontorybe.common.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.common.application.DevTokenInitializer;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.font.FontCreateResendRequestEvent;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.service.dto.FontRequestProduceDto;
import org.fontory.fontorybe.font.service.port.FontRepository;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.service.MemberLookupServiceImpl;
import org.fontory.fontorybe.sms.application.port.PhoneNumberStorage;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.fontory.fontorybe.authentication.application.AuthConstants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DebugController {
    private final FontRepository fontRepository;
    private final CloudStorageService cloudStorageService;
    private final MemberLookupServiceImpl memberLookupService;
    private final DevTokenInitializer devTokenInitializer;
    private final FontRequestProducer fontRequestProducer;
    private final ApplicationEventPublisher eventPublisher;
    private final CookieUtils cookieUtils;
    private final PhoneNumberStorage phoneNumberStorage;

    @Value("${commit.hash}")
    public String commitHash;

    @GetMapping("/health-check")
    public String healthCheck() { return commitHash; }

    @GetMapping("/debug/sqs-test")
    public String sqsTest() {
        FontRequestProduceDto dto = FontRequestProduceDto.builder()
                .memberId(1L)
                .fontId(1L)
                .fontName("testFontName")
                .templateURL("https://fontory-profile-image.s3.ap-northeast-2.amazonaws.com/a88cd9a0-36fb-4a4f-abd8-696e6235a0a9.jpeg")
                .author("testMemberNickname")
                .requestUUID(MDC.get("requestId"))
                .build();
        fontRequestProducer.sendFontRequest(dto);
        return "test";
    }

    @GetMapping("/debug/token-cookies")
    public String cookies(HttpServletRequest request) {
        Optional<String> accessCookie = cookieUtils.extractTokenFromCookieInRequest(request, ACCESS_TOKEN_COOKIE_NAME);
        Optional<String> refreshCookie = cookieUtils.extractTokenFromCookieInRequest(request, REFRESH_TOKEN_COOKIE_NAME);

        String accessToken = accessCookie.orElse(null);
        String refreshToken = refreshCookie.orElse(null);

        return "accessToken: " + accessToken + "\nrefreshToken: " + refreshToken;
    }

    @GetMapping("/debug/auth/me")
    public String me(
            @Login UserPrincipal userPrincipal
    ) {
        return String.valueOf(userPrincipal.getId());
    }

    @GetMapping("/debug/login")
    public void login(HttpServletResponse res) {
        devTokenInitializer.issueTestAccessCookies(res);
    }

    @GetMapping("/debug/logout")
    public void logout(HttpServletResponse res) {
        devTokenInitializer.removeTestAccessCookies(res);
    }

    @PostMapping("/debug/re_request/fonts")
    public Boolean reRequestFonts(
            Long fontId,
            String notificationPhoneNumber
    ) {
        Optional<Font> optionalFont = fontRepository.findById(fontId);
        if (optionalFont.isEmpty()) {
            return false;
        }

        Font savedFont = optionalFont.get();
        Member member = memberLookupService.getOrThrowById(savedFont.getMemberId());

        // SQS 재요청
        String fontPaperUrl = cloudStorageService.getFontPaperUrl(savedFont.getKey());
        fontRequestProducer.sendFontRequest(FontRequestProduceDto.from(savedFont, member, fontPaperUrl));

        // SMS 알림을 위한 레디스에 폰번호 다시 저장
        if (notificationPhoneNumber != null && !notificationPhoneNumber.isBlank()) {
            log.info("redis save start");
            eventPublisher.publishEvent(new FontCreateResendRequestEvent(savedFont, notificationPhoneNumber));
            log.info("redis save end");
        }

        log.info("Response sent: Font created with ID: {}, name: {}, phone: {}",
                savedFont.getId(), savedFont.getName(), notificationPhoneNumber);

        return true;
    }
}
