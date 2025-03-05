package org.fontory.fontorybe.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2FailureHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2SuccessHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2UserService;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtAuthenticationFilter;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtOnlyOAuth2RequireFilter;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOauth2UserService oauth2UserService;
    private final CustomOauth2SuccessHandler oauth2SuccessHandler;
    private final CustomOauth2FailureHandler oauth2FailureHandler;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * "/files/**" 엔드포인트 전용 SecurityFilterChain:
     * - OAUTH2 토큰 검증을 위한 JwtOnlyProvideRequireFilter만 적용되는 컨트롤러
     * 회원가입전 사진업로드(POST, "/files/profile-image"), 회원가입(POST, "/member")
     */
    @Bean
    @Order(1)
    public SecurityFilterChain filesSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // "/files/**" 경로에만 적용되도록 지정
                .securityMatcher(new OrRequestMatcher(
                    new AntPathRequestMatcher("/files/profile-image", HttpMethod.POST.name()),
                    new AntPathRequestMatcher("/member", HttpMethod.POST.name())
                ))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtOnlyOAuth2RequireFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 기본 SecurityFilterChain:
     * - "/files/**" 이외의 모든 요청에 대해 JwtAuthenticationFilter와 OAuth2 관련 설정 적용
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/files/**")))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(oauth2UserService)
                        )
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/health-check",
                        "/auth/token/**"
                );
    }
}