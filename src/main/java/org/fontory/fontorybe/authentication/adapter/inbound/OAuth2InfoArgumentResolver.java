package org.fontory.fontorybe.authentication.adapter.inbound;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.outbound.JwtTokenProvider;
import org.fontory.fontorybe.authentication.domain.exception.TokenNotFoundException;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class OAuth2InfoArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final ProvideService provideService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(OAuth2.class)
                && parameter.getParameterType().equals(Provide.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String token = request.getHeader("Authorization");

        token = token.substring(7);
        System.out.println("token = " + token);
        return provideService.getOrThrownById(jwtTokenProvider.getProvideId(token));
    }
}
