package org.fontory.fontorybe.config;

import java.util.List;

import org.fontory.fontorybe.authentication.adapter.inbound.resolver.LoginMemberArgumentResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * 웹 MVC 관련 설정을 담당하는 Configuration 클래스
 * CORS 설정, Argument Resolver, Interceptor 등록 등을 처리
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final PerformanceInterceptor performanceInterceptor;

    /**
     * @Login 어노테이션을 처리하기 위한 ArgumentResolver 등록
     * JWT 토큰에서 UserPrincipal 객체를 추출하여 컨트롤러 메서드에 주입
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginMemberArgumentResolver);
    }
    
    /**
     * API 성능 모니터링을 위한 Interceptor 등록
     * 1초 이상 소요된 API는 WARN 레벨로 로그 기록
     * actuator, swagger-ui 경로는 제외
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**");
    }

    /**
     * Spring Security에서 사용할 CORS 설정을 Bean으로 제공
     * 개발 환경(localhost)과 프로덕션 도메인을 허용
     * 쿠키 기반 인증을 위해 credentials를 true로 설정
     * 
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "https://www.fontory.co.kr",
                "https://fontory.co.kr",
                "https://fontory.vercel.app",
                "https://api.fontory.co.kr",
                "https://test.api.fontory.co.kr"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);  // 쿠키 전송을 위한 설정

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
