package org.fontory.fontorybe.integration.bookmark.controller;

import jakarta.servlet.http.Cookie;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
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
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createBookmarkTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteBookmarkTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class BookmarkControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private BookmarkService bookmarkService;

    @MockitoBean private CloudStorageService cloudStorageService;

    private String validAccessToken;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(TEST_MEMBER_ID);
        validAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        
        // Mock CloudStorageService for font file URLs
        given(cloudStorageService.getWoff2Url(any())).willReturn(TEST_FILE_URL);
    }

    @Test
    @DisplayName("POST /bookmarks/{fontId} - 북마크 추가 성공")
    void addBookmarkSuccess() throws Exception {
        Long fontId = 998L; // Font ID that exists but is not bookmarked
        
        mockMvc.perform(post("/bookmarks/{fontId}", fontId)
                    .cookie(new Cookie("accessToken", validAccessToken))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.memberId").value(TEST_MEMBER_ID.intValue()))
                .andExpect(jsonPath("$.fontId").value(fontId.intValue()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST /bookmarks/{fontId} - 이미 북마크된 폰트 중복 추가 시 예외")
    void addBookmarkDuplicateFailure() throws Exception {
        Long fontId = 999L; // Font ID that is already bookmarked
        
        mockMvc.perform(post("/bookmarks/{fontId}", fontId)
                    .cookie(new Cookie("accessToken", validAccessToken))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @DisplayName("POST /bookmarks/{fontId} - 존재하지 않는 폰트 북마크 추가 시 예외")
    void addBookmarkNonExistentFontFailure() throws Exception {
        Long nonExistentFontId = NON_EXIST_ID;
        
        mockMvc.perform(post("/bookmarks/{fontId}", nonExistentFontId)
                    .cookie(new Cookie("accessToken", validAccessToken))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @DisplayName("POST /bookmarks/{fontId} - 인증되지 않은 사용자 북마크 추가 시 401")
    void addBookmarkUnauthorized() throws Exception {
        Long fontId = 998L;
        
        mockMvc.perform(post("/bookmarks/{fontId}", fontId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Authentication Required."));
    }

    @Test
    @DisplayName("DELETE /bookmarks/{fontId} - 북마크 삭제 성공")
    void deleteBookmarkSuccess() throws Exception {
        Long fontId = 999L; // Font ID that is bookmarked
        
        mockMvc.perform(delete("/bookmarks/{fontId}", fontId)
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("DELETE /bookmarks/{fontId} - 북마크하지 않은 폰트 삭제 시 예외")
    void deleteBookmarkNotBookmarkedFailure() throws Exception {
        Long fontId = 998L; // Font ID that is not bookmarked
        
        mockMvc.perform(delete("/bookmarks/{fontId}", fontId)
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").exists());
    }

    @Test
    @DisplayName("DELETE /bookmarks/{fontId} - 인증되지 않은 사용자 북마크 삭제 시 401")
    void deleteBookmarkUnauthorized() throws Exception {
        Long fontId = 999L;
        
        mockMvc.perform(delete("/bookmarks/{fontId}", fontId))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Authentication Required."));
    }

    @Test
    @DisplayName("GET /bookmarks - 북마크한 폰트 목록 조회 성공")
    void getBookmarksSuccess() throws Exception {
        mockMvc.perform(get("/bookmarks")
                    .param("page", "0")
                    .param("size", "10")
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1))) // 1 bookmarked font in test data
                .andExpect(jsonPath("$.content[0].id").value(999))
                .andExpect(jsonPath("$.content[0].name").value("테스트폰트"))
                .andExpect(jsonPath("$.content[0].example").value("이것은 테스트용 예제입니다."))
                .andExpect(jsonPath("$.content[0].bookmarked").value(true))
                .andExpect(jsonPath("$.content[0].writerName").value("testMemberNickName"))
                .andExpect(jsonPath("$.content[0].woff").exists())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
                
        verify(cloudStorageService).getWoff2Url(any());
    }

    @Test
    @DisplayName("GET /bookmarks - 키워드로 북마크한 폰트 검색 성공")
    void getBookmarksWithKeywordSuccess() throws Exception {
        mockMvc.perform(get("/bookmarks")
                    .param("page", "0")
                    .param("size", "10")
                    .param("keyword", "테스트")
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("테스트폰트"));
    }

    @Test
    @DisplayName("GET /bookmarks - 키워드로 북마크한 폰트 검색 결과 없음")
    void getBookmarksWithKeywordNoResults() throws Exception {
        mockMvc.perform(get("/bookmarks")
                    .param("page", "0")
                    .param("size", "10")
                    .param("keyword", "존재하지않는키워드")
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));  // 필터링된 결과가 없으므로 0
    }

    @Test
    @DisplayName("GET /bookmarks - 북마크한 폰트 목록 페이징 테스트")
    void getBookmarksPagination() throws Exception {
        mockMvc.perform(get("/bookmarks")
                    .param("page", "0")
                    .param("size", "5")
                    .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /bookmarks - 인증되지 않은 사용자 북마크 목록 조회 시 401")
    void getBookmarksUnauthorized() throws Exception {
        mockMvc.perform(get("/bookmarks")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Authentication Required."));
    }
}