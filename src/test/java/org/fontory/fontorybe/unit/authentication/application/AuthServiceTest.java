package org.fontory.fontorybe.unit.authentication.application;

import jakarta.servlet.http.HttpServletResponse;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.dto.ResponseCookies;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.domain.exception.InvalidRefreshTokenException;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.fontory.fontorybe.unit.mock.FakeCookieUtils;
import org.fontory.fontorybe.unit.mock.FakeJwtTokenProvider;
import org.fontory.fontorybe.unit.mock.FakeMemberLookupService;
import org.fontory.fontorybe.unit.mock.FakeTokenStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class AuthServiceTest {
    
    private AuthService authService;
    private FakeMemberLookupService memberLookupService;
    private FakeCookieUtils cookieUtils;
    private FakeTokenStorage tokenStorage;
    private FakeJwtTokenProvider jwtTokenProvider;
    
    private Member testMember;
    private final Long TEST_MEMBER_ID = 1L;
    private final Long TEST_PROVIDE_ID = 10L;
    private final String TEST_NICKNAME = "testUser";
    private final String TEST_ACCESS_TOKEN = "test_access_token";
    private final String TEST_REFRESH_TOKEN = "test_refresh_token";
    private final String DIFFERENT_REFRESH_TOKEN = "different_refresh_token";
    
    @BeforeEach
    void setUp() {
        memberLookupService = new FakeMemberLookupService();
        cookieUtils = new FakeCookieUtils();
        tokenStorage = new FakeTokenStorage();
        jwtTokenProvider = new FakeJwtTokenProvider();
        
        authService = AuthService.builder()
                .memberLookupService(memberLookupService)
                .cookieUtils(cookieUtils)
                .tokenStorage(tokenStorage)
                .jwtTokenProvider(jwtTokenProvider)
                .build();
        
        // Create test member
        testMember = Member.builder()
                .id(TEST_MEMBER_ID)
                .nickname(TEST_NICKNAME)
                .gender(Gender.MALE)
                .birth(LocalDate.of(1990, 1, 1))
                .provideId(TEST_PROVIDE_ID)
                .status(MemberStatus.ACTIVATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        memberLookupService.addMember(testMember);
    }
    
    @Test
    @DisplayName("issueAuthCookies - success test")
    void issueAuthCookies_Success() {
        // When
        ResponseCookies responseCookies = authService.issueAuthCookies(testMember);
        
        // Then
        assertAll(
                () -> assertThat(responseCookies).isNotNull(),
                () -> assertThat(responseCookies.getAccessTokenCookie()).isNotNull(),
                () -> assertThat(responseCookies.getRefreshTokenCookie()).isNotNull(),
                () -> assertThat(responseCookies.getAccessTokenCookie().getValue()).startsWith("access_"),
                () -> assertThat(responseCookies.getRefreshTokenCookie().getValue()).startsWith("refresh_")
        );
        
        // Verify token is stored
        String storedRefreshToken = tokenStorage.getRefreshToken(testMember);
        assertThat(storedRefreshToken).isEqualTo(responseCookies.getRefreshTokenCookie().getValue());
    }
    
    @Test
    @DisplayName("issueAuthCookies - creates new tokens and stores refresh token")
    void issueAuthCookies_CreatesNewTokensAndStoresRefreshToken() {
        // When
        ResponseCookies responseCookies = authService.issueAuthCookies(testMember);
        
        // Then
        String accessToken = responseCookies.getAccessTokenCookie().getValue();
        String refreshToken = responseCookies.getRefreshTokenCookie().getValue();
        
        assertAll(
                () -> assertThat(jwtTokenProvider.isValidAccessToken(accessToken)).isTrue(),
                () -> assertThat(jwtTokenProvider.isValidRefreshToken(refreshToken)).isTrue(),
                () -> assertThat(jwtTokenProvider.getMemberIdFromAccessToken(accessToken)).isEqualTo(TEST_MEMBER_ID),
                () -> assertThat(jwtTokenProvider.getMemberIdFromRefreshToken(refreshToken)).isEqualTo(TEST_MEMBER_ID),
                () -> assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isTrue(),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isEqualTo(refreshToken)
        );
    }
    
    @Test
    @DisplayName("refreshAuthCookies - success test with valid refresh token")
    void refreshAuthCookies_Success() {
        // Given
        ResponseCookies initialCookies = authService.issueAuthCookies(testMember);
        String validRefreshToken = initialCookies.getRefreshTokenCookie().getValue();
        
        // When
        ResponseCookies refreshedCookies = authService.refreshAuthCookies(TEST_MEMBER_ID, validRefreshToken);
        
        // Then
        assertAll(
                () -> assertThat(refreshedCookies).isNotNull(),
                () -> assertThat(refreshedCookies.getAccessTokenCookie()).isNotNull(),
                () -> assertThat(refreshedCookies.getRefreshTokenCookie()).isNotNull(),
                () -> assertThat(refreshedCookies.getAccessTokenCookie().getValue()).startsWith("access_"),
                () -> assertThat(refreshedCookies.getRefreshTokenCookie().getValue()).startsWith("refresh_")
        );
        
        // Verify new tokens are different from initial ones
        assertAll(
                () -> assertThat(refreshedCookies.getAccessTokenCookie().getValue())
                        .isNotEqualTo(initialCookies.getAccessTokenCookie().getValue()),
                () -> assertThat(refreshedCookies.getRefreshTokenCookie().getValue())
                        .isNotEqualTo(initialCookies.getRefreshTokenCookie().getValue())
        );
        
        // Verify new refresh token is stored
        String newStoredRefreshToken = tokenStorage.getRefreshToken(testMember);
        assertThat(newStoredRefreshToken).isEqualTo(refreshedCookies.getRefreshTokenCookie().getValue());
    }
    
    @Test
    @DisplayName("refreshAuthCookies - throws exception when no stored refresh token exists")
    void refreshAuthCookies_ThrowsExceptionWhenNoStoredRefreshToken() {
        // Given - no refresh token stored for member
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAuthCookies(TEST_MEMBER_ID, TEST_REFRESH_TOKEN))
                .isExactlyInstanceOf(InvalidRefreshTokenException.class);
    }
    
    @Test
    @DisplayName("refreshAuthCookies - throws exception when refresh token mismatch")
    void refreshAuthCookies_ThrowsExceptionWhenRefreshTokenMismatch() {
        // Given
        authService.issueAuthCookies(testMember); // This stores a refresh token
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAuthCookies(TEST_MEMBER_ID, DIFFERENT_REFRESH_TOKEN))
                .isExactlyInstanceOf(InvalidRefreshTokenException.class);
    }
    
    @Test
    @DisplayName("refreshAuthCookies - throws exception when member not found")
    void refreshAuthCookies_ThrowsExceptionWhenMemberNotFound() {
        // Given
        Long nonExistentMemberId = 999L;
        
        // When & Then
        assertThatThrownBy(() -> authService.refreshAuthCookies(nonExistentMemberId, TEST_REFRESH_TOKEN))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("clearAuthCookies - success test")
    void clearAuthCookies_Success() {
        // Given
        authService.issueAuthCookies(testMember); // Store refresh token
        HttpServletResponse response = new MockHttpServletResponse();
        
        // Verify token exists before clearing
        assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isTrue();
        
        // When
        authService.clearAuthCookies(response, TEST_MEMBER_ID);
        
        // Then
        assertAll(
                () -> assertThat(cookieUtils.isClearCookiesCalled()).isTrue(),
                () -> assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isFalse(),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isNull()
        );
    }
    
    @Test
    @DisplayName("clearAuthCookies - throws exception when member not found")
    void clearAuthCookies_ThrowsExceptionWhenMemberNotFound() {
        // Given
        Long nonExistentMemberId = 999L;
        HttpServletResponse response = new MockHttpServletResponse();
        
        // When & Then
        assertThatThrownBy(() -> authService.clearAuthCookies(response, nonExistentMemberId))
                .isExactlyInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("clearAuthCookies - works even when no refresh token stored")
    void clearAuthCookies_WorksEvenWhenNoRefreshTokenStored() {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        
        // Verify no token exists
        assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isFalse();
        
        // When & Then - should not throw exception
        authService.clearAuthCookies(response, TEST_MEMBER_ID);
        
        assertThat(cookieUtils.isClearCookiesCalled()).isTrue();
    }
    
    @Test
    @DisplayName("Token lifecycle - issue, refresh, clear")
    void tokenLifecycle_IssueRefreshClear() {
        // Step 1: Issue initial cookies
        ResponseCookies initialCookies = authService.issueAuthCookies(testMember);
        String initialRefreshToken = initialCookies.getRefreshTokenCookie().getValue();
        
        assertAll(
                () -> assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isTrue(),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isEqualTo(initialRefreshToken)
        );
        
        // Step 2: Refresh cookies
        ResponseCookies refreshedCookies = authService.refreshAuthCookies(TEST_MEMBER_ID, initialRefreshToken);
        String newRefreshToken = refreshedCookies.getRefreshTokenCookie().getValue();
        
        assertAll(
                () -> assertThat(newRefreshToken).isNotEqualTo(initialRefreshToken),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isEqualTo(newRefreshToken)
        );
        
        // Step 3: Clear cookies
        HttpServletResponse response = new MockHttpServletResponse();
        authService.clearAuthCookies(response, TEST_MEMBER_ID);
        
        assertAll(
                () -> assertThat(tokenStorage.hasRefreshToken(TEST_MEMBER_ID)).isFalse(),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isNull(),
                () -> assertThat(cookieUtils.isClearCookiesCalled()).isTrue()
        );
    }
    
    @Test
    @DisplayName("Multiple token issuance - should replace previous tokens")
    void multipleTokenIssuance_ShouldReplacePreviousTokens() {
        // Given
        ResponseCookies firstCookies = authService.issueAuthCookies(testMember);
        String firstRefreshToken = firstCookies.getRefreshTokenCookie().getValue();
        
        // When - issue new cookies
        ResponseCookies secondCookies = authService.issueAuthCookies(testMember);
        String secondRefreshToken = secondCookies.getRefreshTokenCookie().getValue();
        
        // Then
        assertAll(
                () -> assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken),
                () -> assertThat(tokenStorage.getRefreshToken(testMember)).isEqualTo(secondRefreshToken),
                // First token should no longer be valid for refresh
                () -> assertThatThrownBy(() -> authService.refreshAuthCookies(TEST_MEMBER_ID, firstRefreshToken))
                        .isExactlyInstanceOf(InvalidRefreshTokenException.class)
        );
    }
    
    @Test
    @DisplayName("UserPrincipal creation from member")
    void userPrincipalCreationFromMember() {
        // When
        UserPrincipal userPrincipal = UserPrincipal.from(testMember);
        
        // Then
        assertAll(
                () -> assertThat(userPrincipal).isNotNull(),
                () -> assertThat(userPrincipal.getId()).isEqualTo(TEST_MEMBER_ID),
                () -> assertThat(userPrincipal.getUsername()).isEqualTo(TEST_MEMBER_ID.toString())
        );
    }
}