package org.fontory.fontorybe.authentication.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fontory.fontorybe.provide.domain.exception.ProviderNotFoundException;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

import java.util.Map;

@AllArgsConstructor
@Getter
public class Auth2UserInfo {
    private final String UserIdentifier;
    private final Provider provider;
    private final String email;


    public static Auth2UserInfo getOAuth2UserInfo(Provider provider, Map<String, Object> attributes, String pk) {
        switch (provider) {
            case GOOGLE -> {
                return new Auth2UserInfo((String) attributes.get(pk), Provider.GOOGLE, (String) attributes.get("email"));
            }
        }
        throw new ProviderNotFoundException(String.valueOf(provider));
    }
}
