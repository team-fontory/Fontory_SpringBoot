package org.fontory.fontorybe.integration.common.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.common.application.DevTokenInitializer;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.fontory.fontorybe.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DebugController integration tests.
 * Tests debug endpoints including health checks, SQS testing, token handling, and authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DebugControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberLookupService memberLookupService;

    @MockitoBean
    private FontRequestProducer fontRequestProducer;

    @MockitoBean
    private DevTokenInitializer devTokenInitializer;

    private String validAccessToken;
    private Member testMember;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testMember = memberLookupService.getOrThrowById(TEST_MEMBER_ID);
        userPrincipal = UserPrincipal.from(testMember);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);

        // Mock DevTokenInitializer methods
        doNothing().when(devTokenInitializer).issueTestAccessCookies(any());
        doNothing().when(devTokenInitializer).removeTestAccessCookies(any());
    }

    @Test
    @DisplayName("GET /health-check - returns commit hash successfully")
    void testHealthCheck() throws Exception {
        // When: Requesting health check endpoint
        mockMvc.perform(get("/health-check"))
                // Then: Should return 200 OK with commit hash
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("GET /health-check - endpoint is accessible without authentication")
    void testHealthCheckNoAuthRequired() throws Exception {
        // When: Requesting health check without any authentication
        mockMvc.perform(get("/health-check"))
                // Then: Should return 200 OK (no authentication required)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /debug/sqs-test - sends SQS message successfully")
    void testSqsTest() throws Exception {
        // Given: Mock SQS producer
        doNothing().when(fontRequestProducer).sendFontRequest(any());

        // When: Requesting SQS test endpoint
        mockMvc.perform(get("/debug/sqs-test"))
                // Then: Should return 200 OK with test response
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("test"));

        // Verify that SQS producer was called
        verify(fontRequestProducer).sendFontRequest(any());
    }

    @Test
    @DisplayName("GET /debug/sqs-test - endpoint is accessible without authentication")
    void testSqsTestNoAuthRequired() throws Exception {
        // Given: Mock SQS producer
        doNothing().when(fontRequestProducer).sendFontRequest(any());

        // When: Requesting SQS test without authentication
        mockMvc.perform(get("/debug/sqs-test"))
                // Then: Should return 200 OK (no authentication required)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /debug/token-cookies - returns tokens from cookies")
    void testTokenCookiesWithTokens() throws Exception {
        // Given: Valid access and refresh tokens in cookies
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        // When: Requesting token cookies endpoint with tokens
        mockMvc.perform(get("/debug/token-cookies")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                // Then: Should return 200 OK with token information
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("accessToken: " + validAccessToken)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refreshToken: " + refreshToken)));
    }

    @Test
    @DisplayName("GET /debug/token-cookies - returns null when no cookies present")
    void testTokenCookiesWithoutTokens() throws Exception {
        // When: Requesting token cookies endpoint without cookies
        mockMvc.perform(get("/debug/token-cookies"))
                // Then: Should return 200 OK with null values
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("accessToken: null\nrefreshToken: null"));
    }

    @Test
    @DisplayName("GET /debug/token-cookies - handles partial cookies correctly")
    void testTokenCookiesWithPartialTokens() throws Exception {
        // Given: Only access token cookie present
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);

        // When: Requesting token cookies endpoint with only access token
        mockMvc.perform(get("/debug/token-cookies")
                        .cookie(accessTokenCookie))
                // Then: Should return 200 OK with access token and null refresh token
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("accessToken: " + validAccessToken)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("refreshToken: null")));
    }

    @Test
    @DisplayName("GET /debug/token-cookies - endpoint is accessible without authentication")
    void testTokenCookiesNoAuthRequired() throws Exception {
        // When: Requesting token cookies without authentication
        mockMvc.perform(get("/debug/token-cookies"))
                // Then: Should return 200 OK (no authentication required)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /debug/auth/me - returns user ID with valid authentication")
    void testAuthMeWithValidToken() throws Exception {
        // Given: Valid access token in cookie
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);

        // When: Requesting authenticated user info
        mockMvc.perform(get("/debug/auth/me")
                        .cookie(accessTokenCookie))
                // Then: Should return 200 OK with user ID
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(String.valueOf(TEST_MEMBER_ID)));
    }

    @Test
    @DisplayName("GET /debug/auth/me - returns 401 without authentication")
    void testAuthMeWithoutAuthentication() throws Exception {
        // When: Requesting authenticated user info without token
        mockMvc.perform(get("/debug/auth/me"))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /debug/auth/me - returns 401 with invalid token")
    void testAuthMeWithInvalidToken() throws Exception {
        // Given: Invalid access token
        String invalidToken = "invalid.jwt.token";
        Cookie accessTokenCookie = new Cookie("accessToken", invalidToken);

        // When: Requesting authenticated user info with invalid token
        mockMvc.perform(get("/debug/auth/me")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }

    @Test
    @DisplayName("GET /debug/auth/me - returns user ID even for non-existent member (debug behavior)")
    void testAuthMeWithNonExistentMember() throws Exception {
        // Given: Valid JWT token for non-existent member
        UserPrincipal nonExistentUserPrincipal = new UserPrincipal(NON_EXIST_ID);
        String tokenForNonExistentUser = jwtTokenProvider.generateAccessToken(nonExistentUserPrincipal);
        Cookie accessTokenCookie = new Cookie("accessToken", tokenForNonExistentUser);

        // When: Requesting authenticated user info for non-existent member
        mockMvc.perform(get("/debug/auth/me")
                        .cookie(accessTokenCookie))
                // Then: Should return 200 OK with the user ID (debug endpoint doesn't validate member existence)
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string(String.valueOf(NON_EXIST_ID)));
    }

    @Test
    @DisplayName("GET /debug/login - issues test access cookies")
    void testDebugLogin() throws Exception {
        // When: Requesting debug login endpoint
        mockMvc.perform(get("/debug/login"))
                // Then: Should return 200 OK
                .andExpect(status().isOk());

        // Verify that dev token initializer was called
        verify(devTokenInitializer).issueTestAccessCookies(any());
    }

    @Test
    @DisplayName("GET /debug/login - endpoint is accessible without authentication")
    void testDebugLoginNoAuthRequired() throws Exception {
        // When: Requesting debug login without authentication
        mockMvc.perform(get("/debug/login"))
                // Then: Should return 200 OK (no authentication required)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /debug/logout - removes test access cookies")
    void testDebugLogout() throws Exception {
        // When: Requesting debug logout endpoint
        mockMvc.perform(get("/debug/logout"))
                // Then: Should return 200 OK
                .andExpect(status().isOk());

        // Verify that dev token initializer was called
        verify(devTokenInitializer).removeTestAccessCookies(any());
    }

    @Test
    @DisplayName("GET /debug/logout - endpoint is accessible without authentication")
    void testDebugLogoutNoAuthRequired() throws Exception {
        // When: Requesting debug logout without authentication
        mockMvc.perform(get("/debug/logout"))
                // Then: Should return 200 OK (no authentication required)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("All debug endpoints support only GET method")
    void testDebugEndpointsOnlySupportGetMethod() throws Exception {
        // Test health-check endpoint
        mockMvc.perform(post("/health-check"))
                .andExpect(status().isMethodNotAllowed());

        // Test sqs-test endpoint
        mockMvc.perform(post("/debug/sqs-test"))
                .andExpect(status().isMethodNotAllowed());

        // Test token-cookies endpoint
        mockMvc.perform(post("/debug/token-cookies"))
                .andExpect(status().isMethodNotAllowed());

        // Test auth/me endpoint
        mockMvc.perform(post("/debug/auth/me"))
                .andExpect(status().isMethodNotAllowed());

        // Test login endpoint
        mockMvc.perform(post("/debug/login"))
                .andExpect(status().isMethodNotAllowed());

        // Test logout endpoint
        mockMvc.perform(post("/debug/logout"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Debug endpoints handle malformed JWT tokens gracefully")
    void testDebugEndpointsWithMalformedJwtToken() throws Exception {
        // Given: Malformed JWT token
        String malformedToken = "not.a.valid.jwt.token.at.all";
        Cookie accessTokenCookie = new Cookie("accessToken", malformedToken);

        // When: Requesting auth/me with malformed token
        mockMvc.perform(get("/debug/auth/me")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }

    @Test
    @DisplayName("Debug endpoints handle expired JWT tokens gracefully")
    void testDebugEndpointsWithExpiredJwtToken() throws Exception {
        // Given: Expired JWT token (simulated with malformed token)
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.expired";
        Cookie accessTokenCookie = new Cookie("accessToken", expiredToken);

        // When: Requesting auth/me with expired token
        mockMvc.perform(get("/debug/auth/me")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }

}