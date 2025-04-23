package org.fontory.fontorybe.authentication.adapter.inbound.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.config.jwt.JwtProperties;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFontCreateServerFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String uri = request.getRequestURI();
        boolean isFontProgressRequest = "PATCH".equalsIgnoreCase(request.getMethod()) && uri.matches("/fonts/progress/\\d+");
        return !isFontProgressRequest;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        log.info("JwtFontCreateServerFilter");
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendUnauthorized(response, "Missing JWT token");
            return;
        }

        String token = header.substring(7);
        try {
            String fontCreateServer = jwtTokenProvider.getFontCreateServer(token);
            if (!fontCreateServer.equals(jwtProperties.getFontCreateServerSubject())) {
                sendUnauthorized(response, "Invalid or missing token for this request");
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorized(response, "Invalid or missing token for this request");
        }
    }

    private void sendUnauthorized(HttpServletResponse res, String msg) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        BaseErrorResponse body = new BaseErrorResponse(msg);
        res.getWriter().write(objectMapper.writeValueAsString(body));
        res.getWriter().flush();
    }
}
