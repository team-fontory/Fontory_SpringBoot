package org.fontory.fontorybe.authentication.adapter.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final ProvideRepository provideRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 login attempt: provider={}", registrationId);
        
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        log.debug("OAuth2 user loaded from provider: provider={}", registrationId);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Provider provider = Provider.from(userRequest.getClientRegistration().getRegistrationId());
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        Auth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(provider, attributes, userNameAttributeName);
        
        log.info("OAuth2 user info extracted: provider={}, userIdentifier={}, email={}", 
                provider, oAuth2UserInfo.getUserIdentifier(), oAuth2UserInfo.getEmail());

        Provide provide = getProvide(oAuth2UserInfo);
        attributes.put("provide", provide);
        
        log.info("OAuth2 authentication successful: provider={}, provideId={}", 
                provider, provide.getId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }

    private Provide getProvide(Auth2UserInfo oAuth2UserInfo) {
        String userIdentifier = oAuth2UserInfo.getUserIdentifier();
        Provider provider = oAuth2UserInfo.getProvider();
        
        log.debug("Looking up provide info: userIdentifier={}, provider={}", userIdentifier, provider);

        Optional<Provide> oAuthInfo = provideRepository.findByOAuthInfo(userIdentifier, provider);

        if (oAuthInfo.isEmpty() || oAuthInfo.get().getId() == null) {
            log.info("Creating new provide entry: userIdentifier={}, provider={}, email={}", 
                    userIdentifier, provider, oAuth2UserInfo.getEmail());
            
            Provide provide = Provide.builder()
                    .providedId(userIdentifier)
                    .email(oAuth2UserInfo.getEmail())
                    .provider(provider)
                    .build();
            Provide savedProvide = provideRepository.save(provide);
            
            log.info("New provide created: provideId={}, userIdentifier={}, provider={}", 
                    savedProvide.getId(), userIdentifier, provider);
            return savedProvide;
        } else {
            log.info("Existing provide found: provideId={}, userIdentifier={}, provider={}", 
                    oAuthInfo.get().getId(), userIdentifier, provider);
            return oAuthInfo.get();
        }
    }
}
