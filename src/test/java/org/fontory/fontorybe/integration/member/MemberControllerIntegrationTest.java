package org.fontory.fontorybe.integration.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MemberControllerIntegrationTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    /**
     * test values
     */
    private final Long existProvideId = 1L;
    private final Long existMemberId = 999L; // @Sql 로 생성된 기존 회원 ID
    private final String existMemberNickName = "existMemberNickName";
    private final Gender existMemberGender = Gender.MALE;
    private final String existMemberBirth = "2025-01-26";
    private final boolean existMemberTerms = true;
    private final String existMemberProfileImage = "existMemberProfileImage";

    private final Gender newMemberGender = Gender.FEMALE;
    private final LocalDate newMemberBirth = LocalDate.of(2025, 1, 22);
    private final boolean newMemberTerms = false;
    private final String newMemberNickName = "newMemberNickName";
    private final String newMemberProfileImage = "newMemberProfileImage";

    private final boolean updateTerms = false;
    private final String updateNickName = "updateNickName";
    private final String updateProfileImage = "updateProfileImage";

    // 유효한 access token (기존 회원 기준)
    private String validAccessToken;
    private String validProvideToken;

    @BeforeEach
    void setUp() {
        // 이미 존재하는 회원(testMemberId)의 UserPrincipal를 만들어 accessToken을 발급
        UserPrincipal userPrincipal = new UserPrincipal(existMemberId);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        validProvideToken = "Bearer " + jwtTokenProvider.generateTemporalProvideToken(String.valueOf(existProvideId));
    }

    @Test
    @DisplayName("GET /member/check-duplicate - duplicate exists returns true with valid Authorization header")
    void checkDuplicateTrueTest() throws Exception {
        mockMvc.perform(get("/member/check-duplicate")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("nickname", existMemberNickName))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /member/check-duplicate - no duplicate returns false with valid Authorization header")
    void checkDuplicateFalseTest() throws Exception {
        mockMvc.perform(get("/member/check-duplicate")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("nickname", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /member - add member success with valid Authorization header")
    void addMemberSuccessTest() throws Exception {
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(newMemberNickName, newMemberGender, newMemberBirth, newMemberTerms, newMemberProfileImage);
        String jsonRequest = objectMapper.writeValueAsString(memberCreateRequest);

        mockMvc.perform(post("/member")
                        .header("Authorization", validProvideToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /member - update member success with valid Authorization header")
    void updateMemberSuccessTest() throws Exception {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(updateNickName, updateProfileImage, updateTerms);
        String jsonRequest = objectMapper.writeValueAsString(memberUpdateRequest);

        mockMvc.perform(put("/member")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname", is(updateNickName)))
                .andExpect(jsonPath("$.profileImage", is(updateProfileImage)))
                .andExpect(jsonPath("$.terms", is(updateTerms)));
    }

    @Test
    @DisplayName("PUT /member without Authorization header returns 401")
    void putMemberWithoutAuthHeader() throws Exception {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest("updatedNick", "updatedProfile", false);
        String jsonRequest = objectMapper.writeValueAsString(memberUpdateRequest);

        mockMvc.perform(put("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("DELETE /member - disable member success with valid Authorization header")
    void disableMemberSuccessTest() throws Exception {
        mockMvc.perform(delete("/member")
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
    }


    @Test
    @DisplayName("DELETE /member without Authorization header returns 401")
    void deleteMemberWithoutAuthHeader() throws Exception {
        mockMvc.perform(delete("/member"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }
}