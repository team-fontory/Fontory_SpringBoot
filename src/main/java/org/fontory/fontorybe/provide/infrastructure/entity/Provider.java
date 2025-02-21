package org.fontory.fontorybe.provide.infrastructure.entity;

import org.fontory.fontorybe.provide.domain.exception.ProviderNotFoundException;

public enum Provider {
    GOOGLE,
    NAVER;

    public static Provider from(String provider) {
        for (Provider p : Provider.values()) {
            if (p.name().equalsIgnoreCase(provider)) {
                return p;
            }
        }
        throw new ProviderNotFoundException(provider);
    }
}
