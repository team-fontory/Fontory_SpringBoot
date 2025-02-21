package org.fontory.fontorybe.config;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2FailureHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2SuccessHandler;
import org.fontory.fontorybe.authentication.adapter.inbound.CustomOauth2UserService;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtAuthenticationFilter;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOauth2UserService oauth2UserService;
    private final CustomOauth2SuccessHandler oauth2SuccessHandler;
    private final CustomOauth2FailureHandler oauth2FailureHandler;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
//                        .requestMatchers("/health-check").permitAll()
//                        .requestMatchers("/swagger-ui.html").permitAll()
//                        .requestMatchers("/swagger-ui/**").permitAll()
//                        .requestMatchers("/v3/api-docs/**").permitAll()
//                        .requestMatchers("/swagger-resources/**").permitAll()
//                        .requestMatchers("/webjars/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(customConfigure ->
                        customConfigure.successHandler(oauth2SuccessHandler)
                                .failureHandler(oauth2FailureHandler)
//                        .failureUrl("/login?error=true")
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.userService(oauth2UserService))
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
                        "/auth/token/**")
                .requestMatchers(HttpMethod.POST,"/member");
    }
}
