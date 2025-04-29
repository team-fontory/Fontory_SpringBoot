package org.fontory.fontorybe.integration.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
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

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.fontory.fontorybe.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class RegistrationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private ProvideService provideService;
    @Autowired private MemberOnboardService memberOnboardService;

    @MockitoBean private FileService fileService;

    private String validAccessToken;
    private String notInitializedAccessToken;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(TEST_MEMBER_ID);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);

        FileUploadResult mockFileUploadResult = FileUploadResult.builder()
                .fileName(TEST_FILE_NAME)
                .fileUrl(TEST_FILE_URL)
                .fileUploadTime(TEST_FILE_UPLOAD_TIME)
                .size(TEST_FILE_SIZE)
                .build();

        ProvideCreateDto newMemberProvideCreateDto = new ProvideCreateDto(NEW_MEMBER_PROVIDER, NEW_MEMBER_PROVIDED_ID, NEW_MEMBER_EMAIL);
        Provide newProvide = provideService.create(newMemberProvideCreateDto);
        Member notInitedMember = memberOnboardService.fetchOrCreateMember(newProvide);

        notInitializedAccessToken = jwtTokenProvider.generateAccessToken(UserPrincipal.from(notInitedMember));

        given(fileService.uploadProfileImage(any(), any())).willReturn(mockFileUploadResult);
    }

    @Test
    @DisplayName("GET /register/check-duplicate - no duplicate returns false with valid Authorization header")
    void checkDuplicateFalseTest() throws Exception {
        mockMvc.perform(get("/register/check-duplicate")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("nickname", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("GET /register/check-duplicate - duplicate exists returns true with valid Authorization header")
    void checkDuplicateTrueTest() throws Exception {
        mockMvc.perform(get("/register/check-duplicate")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("nickname", TEST_MEMBER_NICKNAME))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("POST /register - add member success with valid Authorization header")
    void addMemberSuccessTest() throws Exception {
        InitMemberInfoRequest initMemberInfoRequest = new InitMemberInfoRequest(NEW_MEMBER_NICKNAME, NEW_MEMBER_GENDER, NEW_MEMBER_BIRTH, NEW_MEMBER_TERMS, NEW_MEMBER_PROFILE_KEY);
        String jsonRequest = objectMapper.writeValueAsString(initMemberInfoRequest);
        MockMultipartFile jsonPart = new MockMultipartFile(
                "req",
                null,
                "application/json",
                jsonRequest.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                TEST_FILE_NAME,
                "image/jpeg",
                "fileBytes".getBytes(StandardCharsets.UTF_8)
        );


        mockMvc.perform(multipart("/register")
                        .file(jsonPart)
                        .file(filePart)
                        .cookie(new Cookie("accessToken", notInitializedAccessToken))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }
}
