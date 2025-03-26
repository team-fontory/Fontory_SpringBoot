package org.fontory.fontorybe.common.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestUUIDFilter implements Filter {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Initializing RequestUUIDFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Check if the request already has an ID (e.g., from an upstream service)
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER_NAME);
            
            // If not, generate a new UUID
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }
            
            // Store the request ID in MDC for logging
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            
            // Add the request ID to the response headers
            httpResponse.addHeader(REQUEST_ID_HEADER_NAME, requestId);
            
            // Log the initial request information
            log.debug("Received request: {} {} (Request ID: {})", 
                    httpRequest.getMethod(), httpRequest.getRequestURI(), requestId);
            
            // Continue with the filter chain
            chain.doFilter(request, response);
        } finally {
            // Always clear the MDC to prevent memory leaks
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying RequestUUIDFilter");
    }
} 