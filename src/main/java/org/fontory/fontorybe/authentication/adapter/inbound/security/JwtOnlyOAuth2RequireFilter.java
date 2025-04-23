package org.fontory.fontorybe.authentication.adapter.inbound.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.application.port.JwtTokenProvider;
import org.fontory.fontorybe.common.domain.BaseErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtOnlyOAuth2RequireFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String uri = request.getRequestURI();
        boolean isFiles = "POST".equalsIgnoreCase(request.getMethod()) && uri.equals("/files/profile-image");
        boolean isPostMember = "POST".equalsIgnoreCase(request.getMethod()) && uri.equals("/member");
        return !(isFiles || isPostMember);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException {
        log.info("JwtOnlyOAuth2RequireFilter");
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
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendUnauthorized(response, e.getMessage());
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
