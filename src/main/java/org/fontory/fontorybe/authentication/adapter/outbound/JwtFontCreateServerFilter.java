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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@RequiredArgsConstructor
public class JwtFontCreateServerFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        boolean isFontProgressRequest = "PATCH".equalsIgnoreCase(request.getMethod()) && uri.matches("/fonts/progress/\\d+");
        return !isFontProgressRequest;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtFontCreateServerFilter");
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing JWT token");
            return;
        }

        String token = header.substring(7);
        try {
            String fontCreateServer = jwtTokenProvider.getFontCreateServer(token);
            if (!fontCreateServer.equals(jwtTokenProvider.getFontCreateServerSubject())) {
                sendUnauthorized(response, "Invalid or missing token for this request");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorized(response, "Invalid or missing token for this request");
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
