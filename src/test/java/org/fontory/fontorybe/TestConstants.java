package org.fontory.fontorybe;

import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TestConstants {
    private TestConstants() {} // 인스턴스 생성 방지

    // NOT created in SQL
    public static final Gender NEW_MEMBER_GENDER       = Gender.FEMALE;
    public static final boolean NEW_MEMBER_TERMS        = false;
    public static final LocalDate NEW_MEMBER_BIRTH      = LocalDate.of(2025, 1, 22);
    public static final String NEW_MEMBER_NICKNAME      = "newMemberNickName";
    public static final String NEW_MEMBER_PROFILE_KEY   = "newMemberProfileImage";

    public static final Provider NEW_MEMBER_PROVIDER    = Provider.NAVER;
    public static final String NEW_MEMBER_PROVIDED_ID   = "newMemberProvidedId";
    public static final String NEW_MEMBER_EMAIL         = "newMemberEmail";

    public static final String DEFAULT_PROFILE_KEY      = "defaultProfileImage";
    // Created in SQL
    public static final Gender TEST_MEMBER_GENDER       = Gender.MALE;
    public static final boolean TEST_MEMBER_TERMS        = true;
    public static final LocalDate TEST_MEMBER_BIRTH      = LocalDate.of(2025, 1, 26);
    public static final String TEST_MEMBER_NICKNAME      = "testMemberNickName";
    public static final String TEST_MEMBER_PROFILE_KEY   = "testMemberProfileImage";

    public static final Provider TEST_MEMBER_PROVIDER    = Provider.GOOGLE;
    public static final String TEST_MEMBER_PROVIDED_ID   = "testMemberProvidedId";
    public static final String TEST_MEMBER_EMAIL         = "testMemberEmail";

    public static final Long TEST_MEMBER_ID            = 999L;
    public static final Long TEST_PROVIDE_ID           = 1L;
    public static final Long NON_EXIST_ID              = -1L;

    public static final boolean UPDATE_MEMBER_TERMS     = Boolean.FALSE;
    public static final String UPDATE_MEMBER_NICKNAME   = "updateMemberNickName";
    public static final String UPDATE_MEMBER_PROFILE_KEY= "updateMemberProfileImage";

    // -- FileService mock 결과 --
    public static final Long TEST_FILE_ID              = 999L;
    public static final String TEST_FILE_NAME           = "testFileName.jpg";
    public static final String TEST_FILE_KEY            = "testFileKey";
    public static final String TEST_FILE_EXTENSION      = "jpg";
    public static final String TEST_FILE_URL            = "testFileUrl";
    public static final LocalDateTime TEST_FILE_UPLOAD_TIME = LocalDateTime.of(2025, 1, 22, 3, 25);
    public static final long TEST_FILE_SIZE             = 15_232L;

    public static final String UPDDATE_FILE_NAME           = "updateFileName";
    public static final String UPDDATE_FILE_URL            = "updateFileUrl";
    public static final LocalDateTime UPDDATE_FILE_UPLOAD_TIME = LocalDateTime.of(2029, 1, 22, 3, 25);
    public static final long UPDDATE_FILE_SIZE             = 15_2232L;


    public static final String TEST_AWS_REGION          = "ap-northeast-2";
    public static final String TEST_CDN_URL             = "https://testcdnurl.com";
    public static final String TEST_PROFILE_BUCKET_NAME   = "testProfileBucketName";
    public static final String TEST_TEMPLATE_BUCKET_NAME  = "testTemplateBucketName";
    public static final String TEST_FONT_BUCKET_NAME       = "testFontBucketName";
    public static final String TEST_PROFILE_PREFIX        = "testProfilePrefix";
    public static final String TEST_TEMPLATE_PREFIX       = "testTemplatePrefix";
    public static final String TEST_FONT_PREFIX       = "testFontPrefix";
}
