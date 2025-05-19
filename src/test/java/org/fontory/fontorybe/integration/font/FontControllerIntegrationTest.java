package org.fontory.fontorybe.integration.font;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.common.application.DevTokenInitializer;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
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
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createFontTestData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteFontTestData.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class FontControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private DevTokenInitializer devTokenInitializer;

    @Autowired
    private CloudStorageService cloudStorageService;

    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private FontRequestProducer fontRequestProducer;

    private final Long existMemberId = 999L;
    private final String existMemberName = "existMemberNickName";

    private final Long existFontId = 999L;
    private final String existFontName = "테스트폰트";
    private final String existFontStatus = "DONE";
    private final String existFontExample = "이것은 테스트용 예제입니다.";
    private final Long existFontDownloadCount = 0L;
    private final Long existFontBookmarkCount = 0L;
    private final String existFontTemplateName = "fontTemplateImage.jpg";
    private final Long existFontSize = 12345L;
    private final String existFontKey = "key";
    private final String existFontTemplateExtension = "jpg";

    private final String newFontName = "제작한글폰트1";
    private final String newFontEngName = "newFontEngName";
    private final String newFontExample = "newFontExample";

    private final String updateFontName = "updateFontName";
    private final String updateFontExample = "updateFontExample";

    private String validAccessToken;
    private String validFontCreateServerToken;

    private FileUploadResult fileDetails;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(existMemberId);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        validFontCreateServerToken = "Bearer " + devTokenInitializer.getFixedTokenForFontCreateServer();

        fileDetails = FileUploadResult.builder()
                .fileName("fontTemplateImage.jpg")
                .fileUrl("https://mock-s3.com/fake.jpg")
                .size(1024L)
                .build();

        FileMetadata fileMetadata = FileMetadata.builder()
                .id(existFontId)
                .fileName(existFontTemplateName)
                .key(existFontKey)
                .extension(existFontTemplateExtension)
                .size(existFontSize)
                .build();

        given(fileService.getOrThrowById(any())).willReturn(fileMetadata);

        given(fileService.uploadFontTemplateImage(any(), any())).willReturn(fileDetails);

        doNothing().when(fontRequestProducer).sendFontRequest(any());
    }

    @Test
    @DisplayName("POST /fonts - add font success with valid Authorization header")
    void addFontSuccess() throws Exception {
        // given
        FontCreateDTO createDTO = FontCreateDTO.builder()
                .name(newFontName)
                .engName(newFontEngName)
                .example(newFontExample)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "fontCreateDTO",
                null,
                "application/json",
                jsonRequest.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "fontTemplateImage.jpg",
                "image/jpeg",
                "<<임시파일바이트>>".getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/fonts")
                        .file(jsonPart)
                        .file(filePart)
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(newFontName)))
                .andExpect(jsonPath("$.status", is("PROGRESS")))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /fonts - add font without Authorization header returns 401")
    void addFontWithoutAuthHeader() throws Exception {
        // given
        FontCreateDTO createDTO = FontCreateDTO.builder()
                .name(newFontName)
                .example(newFontExample)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(createDTO);

        // when & then
        mockMvc.perform(post("/fonts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /fonts/progress - success with valid Authorization header")
    void getFontProgressSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/progress")
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].status").isNotEmpty());
    }

    @Test
    @DisplayName("GET /fonts/progress - getFontProgress without Authorization header returns 401")
    void getFontProgressWithoutAuthHeader() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /fonts/members - success with valid Authorization header")
    void getMyFontsSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/members")
                        .cookie(new Cookie("accessToken", validAccessToken))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].name").isNotEmpty())
                .andExpect(jsonPath("$.content[0].example").isNotEmpty())
                .andExpect(jsonPath("$.content[0].downloadCount").isNumber())
                .andExpect(jsonPath("$.content[0].bookmarkCount").isNumber())
                .andExpect(jsonPath("$.content[0].bookmarked").isBoolean())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /fonts/members - without Authorization header returns 401")
    void getMyFontsWithoutAuthHeader() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/members")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /fonts/{fontId} - get font detail success without authentication")
    void getFontDetailSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/{fontId}", existFontId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existFontId.intValue())))
                .andExpect(jsonPath("$.name", is(existFontName)))
                .andExpect(jsonPath("$.example", is(existFontExample)))
                .andExpect(jsonPath("$.writerName", is(existMemberName)))
                .andExpect(jsonPath("$.downloadCount", is(existFontDownloadCount.intValue())))
                .andExpect(jsonPath("$.bookmarkCount", is(existFontBookmarkCount.intValue())));
    }

    @Test
    @DisplayName("DELETE /fonts/members/{fontId} - success when authorized and own font")
    void deleteFontSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/fonts/members/{fontId}", existFontId)
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existFontId.intValue())));
    }

    @Test
    @DisplayName("DELETE /fonts/members/{fontId} - without Authorization header returns 401")
    void deleteFontWithoutAuthHeader() throws Exception {
        // when & then
        mockMvc.perform(delete("/fonts/members/{fontId}", existFontId))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /fonts - success with valid pagination and optional keyword")
    void getFontPageSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "createdAt")
                        .param("keyword", "")
                        .header("Authorization", validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].name").isNotEmpty())
                .andExpect(jsonPath("$.content[0].example").isNotEmpty())
                .andExpect(jsonPath("$.content[0].writerName").isNotEmpty())
                .andExpect(jsonPath("$.content[0].downloadCount").isNumber())
                .andExpect(jsonPath("$.content[0].bookmarkCount").isNumber())
                .andExpect(jsonPath("$.content[0].bookmarked").isBoolean())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /fonts/{fontId}/others - success")
    void getOtherFontsByWriterSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/fonts/{fontId}/others", existFontId)
                        .header("Authorization", validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(lessThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("GET /fonts/members/popular - success with valid Authorization header")
    void getMyPopularFontsSuccess() throws Exception {
        mockMvc.perform(get("/fonts/members/popular")
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("GET /fonts/members/popular - without Authorization header returns 401")
    void getMyPopularFontsWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/fonts/members/popular"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /fonts/popular - success with valid Authorization header")
    void getPopularFontsSuccess() throws Exception {
        mockMvc.perform(get("/fonts/popular")
                        .header("Authorization", validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("PATCH /fonts/progress/{fontId} - success")
    void updateFontProgressSuccess() throws Exception {
        FontProgressUpdateDTO updateDTO = FontProgressUpdateDTO.builder()
                .status(FontStatus.DONE)
                .build();

        String content = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(patch("/fonts/progress/{fontId}", 999L)
                        .header("Authorization", validFontCreateServerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    @DisplayName("GET /fonts/{fontId}/download - success")
    void downloadFontSuccess() throws Exception {
        mockMvc.perform(get("/fonts/{fontId}/download", 999L)
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(999)))
                .andExpect(jsonPath("$.name", is(existFontName)))
                .andExpect(jsonPath("$.ttf", is(cloudStorageService.getTtfUrl(existFontKey))));
    }

    @Test
    @DisplayName("GET /fonts/{fontId}/download - without Authorization header returns 401")
    void downloadFontWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/fonts/{fontId}/download", 999L))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage").value("Authentication Required."));
    }
}
