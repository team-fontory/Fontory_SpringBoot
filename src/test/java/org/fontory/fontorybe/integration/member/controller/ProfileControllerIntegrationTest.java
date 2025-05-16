package org.fontory.fontorybe.integration.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.fontory.fontorybe.TestConstants.*;
import static org.fontory.fontorybe.TestConstants.UPDATE_MEMBER_TERMS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ProfileControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private MemberLookupService memberLookupService;

    @MockitoBean private FileService fileService;

    private String validAccessToken;

    Member testMember;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(TEST_MEMBER_ID);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        testMember = memberLookupService.getOrThrowById(TEST_MEMBER_ID);

        FileUploadResult mockFileUploadResult = FileUploadResult.builder()
                .fileName(TEST_FILE_NAME)
                .fileUrl(TEST_MEMBER_PROFILE_KEY)
                .fileUploadTime(TEST_FILE_UPLOAD_TIME)
                .size(TEST_FILE_SIZE)
                .build();

        given(fileService.uploadProfileImage(any(), any())).willReturn(mockFileUploadResult);
    }

    @Test
    @DisplayName("GET /register/check-duplicate - no duplicate returns false with valid Authorization JWT Cookie")
    void checkDuplicateFalseTest() throws Exception {
        mockMvc.perform(get("/register/check-duplicate")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("nickname", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("PUT /member - update member success with valid Authorization JWT Cookie")
    void updateMemberSuccessTest() throws Exception {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME, UPDATE_MEMBER_TERMS);
        String jsonRequest = objectMapper.writeValueAsString(memberUpdateRequest);
        MockMultipartFile jsonPart = new MockMultipartFile(
                "req",
                null,
                "application/json",
                jsonRequest.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                UPDDATE_FILE_NAME,
                "image/jpeg",
                "fileBytes".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartHttpServletRequestBuilder builder = multipart("/member/me");
        builder.with(request -> { request.setMethod("PATCH"); return request; });

        mockMvc.perform(builder
                        .file(jsonPart)
                        .file(filePart)
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId", is(TEST_MEMBER_ID.intValue())))
                .andExpect(jsonPath("$.nickname", is(UPDATE_MEMBER_NICKNAME)))
                .andExpect(jsonPath("$.terms", is(UPDATE_MEMBER_TERMS)))
                .andExpect(jsonPath("$.profileImageUrl", containsString(testMember.getProfileImageKey())))
                .andExpect(jsonPath("$.gender", is(testMember.getGender().name())))
                .andExpect(jsonPath("$.birth", is(testMember.getBirth().toString())));
    }

    @Test
    @DisplayName("PUT /member without Authorization JWT Cookie returns 401")
    void putMemberWithoutAuthHeader() throws Exception {
        MemberUpdateRequest memberUpdateRequest = new MemberUpdateRequest(UPDATE_MEMBER_NICKNAME, UPDATE_MEMBER_TERMS);
        String jsonRequest = objectMapper.writeValueAsString(memberUpdateRequest);

        mockMvc.perform(put("/member/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("DELETE /member - disable member success with valid Authorization JWT Cookie")
    void disableMemberSuccessTest() throws Exception {
        mockMvc.perform(delete("/member/me")
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
    }


    @Test
    @DisplayName("DELETE /member without Authorization JWT Cookie returns 401")
    void deleteMemberWithoutAuthHeader() throws Exception {
        mockMvc.perform(delete("/member/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }
}
