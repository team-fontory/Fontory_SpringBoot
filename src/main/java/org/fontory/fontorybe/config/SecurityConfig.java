package org.fontory.fontorybe.config;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2FailureHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2SuccessHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2UserService;
import org.fontory.fontorybe.authentication.adapter.inbound.security.JwtAuthenticationFilter;
import org.fontory.fontorybe.authentication.adapter.inbound.security.JwtFontCreateServerFilter;
import org.fontory.fontorybe.authentication.application.AuthService;
import org.fontory.fontorybe.authentication.application.port.CookieUtils;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.config.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOauth2UserService oauth2UserService;
    private final CustomOauth2SuccessHandler oauth2SuccessHandler;
    private final CustomOauth2FailureHandler oauth2FailureHandler;

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthService authService;
    private final CookieUtils cookieUtils;

    /**
     * 0. Chain for Swagger Oauth2
     */
    @Bean
    @Order(0)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/swagger-ui/**"),
                        new AntPathRequestMatcher("/swagger-ui.html"),
                        new AntPathRequestMatcher("/v3/api-docs/**"),
                        new AntPathRequestMatcher("/swagger-resources/**"),
                        new AntPathRequestMatcher("/webjars/**")
                ))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * 1. Chain for OAUTH2
     */
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/oauth2/**"),
                        new AntPathRequestMatcher("/login/oauth2/**")
                ))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureHandler(oauth2FailureHandler)
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(oauth2UserService))
                )
                .build();
    }

    /**
     * 2. FontCreateServer(FastAPI)에서 폰트제작상태 update 요청을 검증
     */
    @Bean
    @Order(2)
    public SecurityFilterChain fontCreateServerSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/fonts/progress/{fontId:[\\d]+}", HttpMethod.PATCH.name())
                ))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new JwtFontCreateServerFilter(jwtTokenProvider, jwtProperties), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 3. defaultSecurityFilterChain
     * 그 외의 모든 요청에 대해 JWT 인증 필터 적용
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(
                        new OrRequestMatcher(
                                new NegatedRequestMatcher(new AntPathRequestMatcher("/debug/**")),
                                new AntPathRequestMatcher("/debug/auth/me", HttpMethod.GET.name())
                ))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
                .addFilterAfter(new JwtAuthenticationFilter(jwtTokenProvider, authService, cookieUtils), ExceptionTranslationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // 인증이 없어도 되는 (@Login(required= false) 가능) 요청 엔드포인트
                        .requestMatchers(HttpMethod.GET, "/fonts/{fontId:[\\d]+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fonts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fonts/{fontId:[\\d]+}/others").permitAll()
                        .requestMatchers(HttpMethod.GET, "/fonts/popular").permitAll()
                        // 그 외엔 인증 필요
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/health-check",
                        "/actuator/prometheus"
                );
    }
}