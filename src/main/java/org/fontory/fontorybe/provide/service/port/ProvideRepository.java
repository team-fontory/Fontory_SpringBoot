package org.fontory.fontorybe.provide.service.port;

import java.util.Optional;
import org.fontory.fontorybe.provide.domain.Provide;

public interface ProvideRepository {
    Optional<Provide> findById(Long id);
    Provide save(Provide provide);
}
