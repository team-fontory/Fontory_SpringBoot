package org.fontory.fontorybe.provide.service.port;

import java.util.Optional;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

public interface ProvideRepository {
    Optional<Provide> findById(Long id);
    Optional<Provide> findByOAuthInfo(String userIdentifier, Provider provider);
    Provide save(Provide provide);
}
