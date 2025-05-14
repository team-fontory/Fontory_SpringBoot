package org.fontory.fontorybe.authentication.adapter.inbound.resolver;

import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.authentication.domain.exception.TokenNotFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Login.class)
                && parameter.getParameterType().equals(UserPrincipal.class);
    }

    /**
     * 인증이 필요하지 않은(@Login(required = false)) 엔드포인트라면, SecurityContext에 인증정보가 없는경우 NULL을 반환
     * 인증이 반드시 필요한(@Login(required = true)) 엔드포인트라면, SecurityContext에 올바른 UserPrincipal이 존재하지 않으면 예외 반환
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Login loginAnnotation = parameter.getParameterAnnotation(Login.class);
        boolean isRequired = loginAnnotation.required();

        /**
         * 시큐리티 필터를 제대로 통과했는가
         * 1. 토큰 없이 통과
         * 2. 정상적인 토큰과 함께 통과
         */
        if (authentication == null || !authentication.isAuthenticated()) {
            if (isRequired) {
                throw new TokenNotFoundException();
            }
            return null;
        }

        /**
         * 시큐리티 필터를 정상적으로 통과했을 때 Principal 값
         * 1. 토큰없이 통과 -> String 타입 annoymousUser
         * 2. 정상적인 토큰과 함께 통과 -> 사용자 지정 UserPrincipal
         */
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            // 인증이 되어있지만 AnonymousAuthenticationToken 등으로 인해 principal이 UserPrincipal이 아닐 경우
            if (isRequired) {
                throw new TokenNotFoundException();
            }
            return null;
        }

        return principal;
    }
}
