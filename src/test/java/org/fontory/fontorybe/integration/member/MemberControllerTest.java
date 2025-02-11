package org.fontory.fontorybe.integration.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MemberControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private final Long testMemberId = 999L;
    private final String testNickName = "nickname";
    private final Gender testGender = Gender.MALE;
    private final String testBirth = "2025-01-26";
    private final boolean testTerms = true;
    private final String testProfileImage = "testProfileImage";

    @Test
    @DisplayName("GET /member/check-duplicate - duplicate exists returns true")
    void checkDuplicateTrueTest() throws Exception {
        //given

        //when
        mockMvc.perform(get("/member/check-duplicate")
                        .param("nickname", testNickName))
                //then
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("GET /member/check-duplicate - no duplicate returns false")
    void checkDuplicateFalseTest() throws Exception {
        //given

        //when
        mockMvc.perform(get("/member/check-duplicate")
                        .param("nickname", "nonExistentNickname"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("POST /member - add member success")
    void addMemberSuccessTest() throws Exception {
        //given
        MemberCreate memberCreate = new MemberCreate(
                "newMember",
                testGender,
                LocalDate.parse(testBirth),
                testTerms,
                "newProfileImage"
        );
        String jsonRequest = objectMapper.writeValueAsString(memberCreate);
        //when
        mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                //then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nickname").value("newMember"));
    }

    @Test
    @DisplayName("POST /member - add member duplicate nickname failure")
    void addMemberDuplicateNicknameTest() throws Exception {
        //given
        MemberCreate memberCreate = new MemberCreate(
                testNickName,
                testGender,
                LocalDate.parse(testBirth),
                testTerms,
                "profileImage"
        );
        String jsonRequest = objectMapper.writeValueAsString(memberCreate);
        //when
        mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /member/{memberId} - update member success")
    void updateMemberSuccessTest() throws Exception {
        //given
        MemberUpdate memberUpdate = new MemberUpdate(
                "updatedNick",
                "updatedProfileImage",
                false
        );
        String jsonRequest = objectMapper.writeValueAsString(memberUpdate);
        //when
        mockMvc.perform(put("/member/{memberId}", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("updatedNick"))
                .andExpect(jsonPath("$.profileImage").value("updatedProfileImage"))
                .andExpect(jsonPath("$.terms").value(false));
    }

    @Test
    @DisplayName("PUT /member/{memberId} - update member duplicate nickname failure")
    void updateMemberDuplicateNicknameTest() throws Exception {
        //given
        MemberCreate memberCreate = new MemberCreate(
                "uniqueMember",
                testGender,
                LocalDate.parse(testBirth),
                testTerms,
                "profileImage"
        );
        String createJson = objectMapper.writeValueAsString(memberCreate);
        String responseContent = mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number newMemberIdNumber = JsonPath.read(responseContent, "$.id");
        Long newMemberId = newMemberIdNumber.longValue();
        MemberUpdate memberUpdate = new MemberUpdate(
                testNickName,
                "newProfileImage",
                true
        );
        String updateJson = objectMapper.writeValueAsString(memberUpdate);
        //when
        mockMvc.perform(put("/member/{memberId}", newMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /member/{memberId} - update non-existent member failure")
    void updateMemberNotFoundTest() throws Exception {
        //given
        MemberUpdate memberUpdate = new MemberUpdate(
                "anyNick",
                "anyProfile",
                true
        );
        String updateJson = objectMapper.writeValueAsString(memberUpdate);
        //when
        mockMvc.perform(put("/member/{memberId}", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /member/{memberId} - disable member success")
    void disableMemberSuccessTest() throws Exception {
        //given
        MemberCreate memberCreate = new MemberCreate(
                "disableTestMember",
                testGender,
                LocalDate.parse(testBirth),
                testTerms,
                "profileImage"
        );
        String createJson = objectMapper.writeValueAsString(memberCreate);
        String responseContent = mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number memberIdToDisableNumber = JsonPath.read(responseContent, "$.id");
        Long memberIdToDisable = memberIdToDisableNumber.longValue();
        //when
        mockMvc.perform(delete("/member/{memberId}", memberIdToDisable))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /member/{memberId} - disable non-existent member failure")
    void disableMemberNotFoundTest() throws Exception {
        //given

        //when
        mockMvc.perform(delete("/member/{memberId}", -1L))
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /member/{memberId} - disable already disabled member failure")
    void disableMemberAlreadyDisabledTest() throws Exception {
        //given
        MemberCreate memberCreate = new MemberCreate(
                "disableTwiceMember",
                testGender,
                LocalDate.parse(testBirth),
                testTerms,
                "profileImage"
        );
        String createJson = objectMapper.writeValueAsString(memberCreate);
        String responseContent = mockMvc.perform(post("/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number memberIdToDisableNumber = JsonPath.read(responseContent, "$.id");
        Long memberIdToDisable = memberIdToDisableNumber.longValue();
        mockMvc.perform(delete("/member/{memberId}", memberIdToDisable))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
        //when
        mockMvc.perform(delete("/member/{memberId}", memberIdToDisable))
                //then
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorMessage").isNotEmpty());
    }
}
