package org.fontory.fontorybe.authentication.adapter.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter");
        String header = request.getHeader("Authorization");
        // 토큰이 없으면 필터 체인을 그대로 진행.
        if (header == null || !header.startsWith("Bearer ")) {
            log.info("Skipping JWT token authentication because Authorization header is missing");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            log.info("Validating JWT token");
            String token = header.substring(7);
            log.info("token: {}", token);
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            log.info("auth: {}", auth.toString());
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Successfully authenticated user");
        } catch (Exception e) {
            log.error("Failed to validate JWT token", e);
            // 토큰이 존재하지만 유효하지 않은 경우엔 에러 응답을 처리.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            BaseErrorResponse errorResponse = new BaseErrorResponse(e.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String jsonError = mapper.writeValueAsString(errorResponse);

            try (PrintWriter writer = response.getWriter()) {
                writer.write(jsonError);
                writer.flush();
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}
