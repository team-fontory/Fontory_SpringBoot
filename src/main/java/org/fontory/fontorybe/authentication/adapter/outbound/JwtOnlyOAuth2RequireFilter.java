package org.fontory.fontorybe.authentication.adapter.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class JwtOnlyOAuth2RequireFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // "/files"로 시작하는 요청에만 필터를 적용
        return !request.getRequestURI().startsWith("/files");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing JWT token");
            return;
        }

        String token = header.substring(7);
        try {
            Long provideId = jwtTokenProvider.getProvideId(token);
            if (provideId == null) {
                sendUnauthorized(response, "Invalid or missing provideId in token");
                return;
            }
            System.out.println("provideId = " + provideId);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorized(response, e.getMessage());
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        BaseErrorResponse errorResponse = new BaseErrorResponse(errorMessage);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String jsonError = mapper.writeValueAsString(errorResponse);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonError);
            writer.flush();
        }
    }
}
