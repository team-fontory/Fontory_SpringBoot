package org.fontory.fontorybe.config;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Only time controller methods (not resources, etc.)
        if (handler instanceof HandlerMethod) {
            request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Only log execution time for controller methods
        if (handler instanceof HandlerMethod handlerMethod) {
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                String controllerName = handlerMethod.getMethod().getDeclaringClass().getSimpleName();
                String methodName = handlerMethod.getMethod().getName();
                String path = request.getRequestURI();
                String method = request.getMethod();
                int status = response.getStatus();
                
                // Log detailed performance data
                if (executionTime > 1000) {
                    // Log slow API calls (> 1 second) at WARN level
                    log.warn("SLOW API: {} {} (Status {}): {}ms - {}.{}", 
                            method, path, status, executionTime, controllerName, methodName);
                } else {
                    // Log normal API calls at DEBUG level
                    log.debug("API PERF: {} {} (Status {}): {}ms - {}.{}", 
                            method, path, status, executionTime, controllerName, methodName);
                }
            }
        }
    }
} 