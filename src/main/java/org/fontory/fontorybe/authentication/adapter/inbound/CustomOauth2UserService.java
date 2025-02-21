package org.fontory.fontorybe.authentication.adapter.inbound;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.domain.Auth2UserInfo;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.fontory.fontorybe.authentication.domain.Auth2UserInfo.getOAuth2UserInfo;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final ProvideRepository provideRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Provider provider = Provider.from(userRequest.getClientRegistration().getRegistrationId());
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Auth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(provider, attributes, userNameAttributeName);

        Provide provide = getProvide(oAuth2UserInfo);
        attributes.put("provide", provide);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }

    private Provide getProvide(Auth2UserInfo oAuth2UserInfo) {
        String userIdentifier = oAuth2UserInfo.getUserIdentifier();
        Provider provider = oAuth2UserInfo.getProvider();

        Optional<Provide> oAuthInfo = provideRepository.findByOAuthInfo(userIdentifier, provider);

        if (oAuthInfo.isEmpty() || oAuthInfo.get().getId() == null) {
            Provide provide = Provide.builder()
                    .providedId(userIdentifier)
                    .email(oAuth2UserInfo.getEmail())
                    .provider(provider)
                    .build();
            return provideRepository.save(provide);
        } else {
            return oAuthInfo.get();
        }
    }
}
