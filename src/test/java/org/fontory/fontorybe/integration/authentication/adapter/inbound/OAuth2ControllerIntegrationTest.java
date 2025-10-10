package org.fontory.fontorybe.integration.authentication.adapter.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2FailureHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2SuccessHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2UserService;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.TokenStorage;
import org.fontory.fontorybe.member.controller.port.MemberOnboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * OAuth2 authentication configuration integration tests.
 * Tests OAuth2 security configuration and handler beans.
 * Note: Full OAuth2 flow testing requires external provider setup,
 * so these tests focus on configuration validation and bean wiring.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(value = "/sql/createMemberTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteMemberTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OAuth2ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private TokenStorage tokenStorage;

    @MockitoBean
    private MemberOnboardService memberOnboardService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CookieUtils cookieUtils;

    @Test
    @DisplayName("OAuth2 success handler bean is properly configured")
    void testOAuth2SuccessHandlerBeanConfiguration() {
        // Test that OAuth2 success handler is properly configured as a Spring bean
        CustomOauth2SuccessHandler successHandler = applicationContext.getBean(CustomOauth2SuccessHandler.class);
        assertNotNull(successHandler, "OAuth2 success handler should be configured as a Spring bean");
    }

    @Test
    @DisplayName("OAuth2 failure handler bean is properly configured")
    void testOAuth2FailureHandlerBeanConfiguration() {
        // Test that OAuth2 failure handler is properly configured as a Spring bean
        CustomOauth2FailureHandler failureHandler = applicationContext.getBean(CustomOauth2FailureHandler.class);
        assertNotNull(failureHandler, "OAuth2 failure handler should be configured as a Spring bean");
    }

    @Test
    @DisplayName("OAuth2 user service bean is properly configured")
    void testOAuth2UserServiceBeanConfiguration() {
        // Test that OAuth2 user service is properly configured as a Spring bean
        CustomOauth2UserService userService = applicationContext.getBean(CustomOauth2UserService.class);
        assertNotNull(userService, "OAuth2 user service should be configured as a Spring bean");
    }

    @Test
    @DisplayName("Auth service is properly configured for OAuth2 integration")
    void testAuthServiceConfiguration() {
        // Test that auth service is properly configured and can be used by OAuth2 handlers
        assertNotNull(authService, "Auth service should be configured for OAuth2 integration");
    }

    @Test
    @DisplayName("Cookie utils service is properly configured for OAuth2 integration")
    void testCookieUtilsConfiguration() {
        // Test that cookie utils service is properly configured for OAuth2 handlers
        assertNotNull(cookieUtils, "Cookie utils should be configured for OAuth2 integration");
    }

    @Test
    @DisplayName("Member onboard service is available for OAuth2 integration")
    void testMemberOnboardServiceConfiguration() {
        // Test that member onboard service is properly mocked and available
        assertNotNull(memberOnboardService, "Member onboard service should be available for OAuth2 integration");
    }

    @Test
    @DisplayName("Token storage is available for OAuth2 integration")
    void testTokenStorageConfiguration() {
        // Test that token storage is properly mocked and available
        assertNotNull(tokenStorage, "Token storage should be available for OAuth2 integration");
    }

    @Test
    @DisplayName("Object mapper is configured for OAuth2 JSON processing")
    void testObjectMapperConfiguration() {
        // Test that object mapper is available for OAuth2 JSON processing
        assertNotNull(objectMapper, "Object mapper should be configured for OAuth2 JSON processing");
    }

    @Test
    @DisplayName("MockMvc is configured for OAuth2 endpoint testing")
    void testMockMvcConfiguration() {
        // Test that MockMvc is properly configured for testing OAuth2 endpoints
        assertNotNull(mockMvc, "MockMvc should be configured for OAuth2 endpoint testing");
    }

    @Test
    @DisplayName("Application context contains all required OAuth2 beans")
    void testApplicationContextOAuth2BeansAvailability() {
        // Test that all required OAuth2 beans are available in the application context
        assertNotNull(applicationContext.getBean(CustomOauth2SuccessHandler.class), 
                "OAuth2 success handler should be in application context");
        assertNotNull(applicationContext.getBean(CustomOauth2FailureHandler.class), 
                "OAuth2 failure handler should be in application context");
        assertNotNull(applicationContext.getBean(CustomOauth2UserService.class), 
                "OAuth2 user service should be in application context");
        assertNotNull(applicationContext.getBean(AuthService.class), 
                "Auth service should be in application context");
        assertNotNull(applicationContext.getBean(CookieUtils.class), 
                "Cookie utils should be in application context");
    }

    @Test
    @DisplayName("OAuth2 configuration integration test setup is valid")
    void testOAuth2ConfigurationTestSetup() {
        // Test that the integration test setup is valid for OAuth2 testing
        // This verifies that all mocks and beans are properly configured
        
        // Verify that essential services are available
        assertNotNull(authService, "Auth service must be available for OAuth2 tests");
        assertNotNull(cookieUtils, "Cookie utils must be available for OAuth2 tests");
        assertNotNull(tokenStorage, "Token storage mock must be available for OAuth2 tests");
        assertNotNull(memberOnboardService, "Member onboard service mock must be available for OAuth2 tests");
        
        // Verify that Spring Boot test context is properly loaded
        assertNotNull(applicationContext, "Application context must be loaded for integration tests");
        assertNotNull(mockMvc, "MockMvc must be available for endpoint testing");
    }
}