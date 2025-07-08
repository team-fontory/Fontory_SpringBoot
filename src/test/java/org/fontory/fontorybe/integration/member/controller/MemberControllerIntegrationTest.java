package org.fontory.fontorybe.integration.member.controller;

import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;


import static org.fontory.fontorybe.TestConstants.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MemberControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private MemberLookupService memberLookupService;

    @MockitoBean private CloudStorageService cloudStorageService;

    /**
     * test values
     */
    // 유효한 access token (기존 회원 기준)
    String validAccessToken;
    Member testMember;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(TEST_MEMBER_ID);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        testMember = memberLookupService.getOrThrowById(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("GET /member/{id} - authenticated returns 200 and ProfileResponse")
    void getInfoMemberSuccess() throws Exception {
        mockMvc.perform(get("/member/{id}", TEST_MEMBER_ID)
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.memberId").value(TEST_MEMBER_ID.intValue()))
                .andExpect(jsonPath("$.nickname").value(TEST_MEMBER_NICKNAME));
    }

    @Test
    @DisplayName("GET /member/{id} - unauthenticated returns 401")
    void getInfoMemberUnauthorized() throws Exception {
        mockMvc.perform(get("/member/{id}", TEST_MEMBER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Authentication Required."));
    }
}