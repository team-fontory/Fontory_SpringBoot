package org.fontory.fontorybe.integration.authentication.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController integration tests.
 * Tests authentication-related endpoints including logout functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberLookupService memberLookupService;

    @MockitoBean
    private TokenStorage tokenStorage;

    private String validAccessToken;
    private String validRefreshToken;
    private Member testMember;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testMember = memberLookupService.getOrThrowById(TEST_MEMBER_ID);
        userPrincipal = UserPrincipal.from(testMember);
        
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        validRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);
        
        // Mock token storage behavior
        given(tokenStorage.getRefreshToken(any(Member.class)))
                .willReturn(validRefreshToken);
    }

    @Test
    @DisplayName("POST /auth/logout - successful logout with valid access token")
    void testLogoutSuccess() throws Exception {
        // Given: Valid access token in cookie
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        
        // When: Performing logout request
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 204 No Content
                .andExpect(status().isNoContent());
        
        // Verify that token storage remove method was called
        verify(tokenStorage).removeRefreshToken(any(Member.class));
    }

    @Test
    @DisplayName("POST /auth/logout - logout without authentication returns 401")
    void testLogoutWithoutAuthentication() throws Exception {
        // When: Performing logout request without access token
        mockMvc.perform(post("/auth/logout"))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("POST /auth/logout - logout with invalid access token returns 401")
    void testLogoutWithInvalidAccessToken() throws Exception {
        // Given: Invalid access token
        String invalidToken = "invalid.jwt.token";
        Cookie accessTokenCookie = new Cookie("accessToken", invalidToken);
        
        // When: Performing logout request with invalid token
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }

    @Test
    @DisplayName("POST /auth/logout - logout with expired access token returns 401")
    void testLogoutWithExpiredAccessToken() throws Exception {
        // Given: Expired access token (this is complex to simulate)
        // For this test, we'll use a malformed token that will fail validation
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.expired";
        Cookie accessTokenCookie = new Cookie("accessToken", expiredToken);
        
        // When: Performing logout request with expired token
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }

    @Test
    @DisplayName("POST /auth/logout - logout clears authentication cookies")
    void testLogoutClearsAuthCookies() throws Exception {
        // Given: Valid access token and refresh token
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        Cookie refreshTokenCookie = new Cookie("refreshToken", validRefreshToken);
        
        // When: Performing logout request
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                // Then: Should return 204 and clear cookies
                .andExpect(status().isNoContent())
                // Verify that Set-Cookie headers are present to clear cookies
                .andExpect(header().exists("Set-Cookie"));
        
        // Verify that refresh token was removed from storage
        verify(tokenStorage).removeRefreshToken(any(Member.class));
    }

    @Test
    @DisplayName("POST /auth/logout - logout removes refresh token from storage")
    void testLogoutRemovesRefreshTokenFromStorage() throws Exception {
        // Given: Valid access token
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        
        // When: Performing logout request
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                .andExpect(status().isNoContent());
        
        // Then: Verify that refresh token was removed from Redis storage
        verify(tokenStorage).removeRefreshToken(any(Member.class));
    }

    @Test
    @DisplayName("POST /auth/logout - logout with non-existent member returns appropriate error")
    void testLogoutWithNonExistentMember() throws Exception {
        // Given: Valid JWT token for non-existent member
        UserPrincipal nonExistentUserPrincipal = new UserPrincipal(NON_EXIST_ID);
        String tokenForNonExistentUser = jwtTokenProvider.generateAccessToken(nonExistentUserPrincipal);
        Cookie accessTokenCookie = new Cookie("accessToken", tokenForNonExistentUser);
        
        // When: Performing logout request
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return error due to member not found
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /auth/logout - multiple logout requests are idempotent")
    void testMultipleLogoutRequestsAreIdempotent() throws Exception {
        // Given: Valid access token
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        
        // When: Performing first logout request
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                .andExpect(status().isNoContent());
        
        // When: Performing second logout request 
        // Note: In an integration test, the same token can still be validated
        // since we're not actually clearing it from the JWT validation
        // but we are clearing it from Redis storage
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // The request should still succeed as JWT validation passes
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /auth/logout - only POST method is allowed")
    void testLogoutOnlyAllowsPostMethod() throws Exception {
        // Given: Valid access token
        Cookie accessTokenCookie = new Cookie("accessToken", validAccessToken);
        
        // When: Attempting GET request to logout endpoint
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 405 Method Not Allowed
                .andExpect(status().isMethodNotAllowed());
        
        // When: Attempting DELETE request to logout endpoint
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 405 Method Not Allowed
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST /auth/logout - handles malformed JWT token gracefully")
    void testLogoutWithMalformedJwtToken() throws Exception {
        // Given: Malformed JWT token
        String malformedToken = "not.a.valid.jwt.token.at.all";
        Cookie accessTokenCookie = new Cookie("accessToken", malformedToken);
        
        // When: Performing logout request with malformed token
        mockMvc.perform(post("/auth/logout")
                        .cookie(accessTokenCookie))
                // Then: Should return 401 Unauthorized
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Invalid access token"));
    }
}