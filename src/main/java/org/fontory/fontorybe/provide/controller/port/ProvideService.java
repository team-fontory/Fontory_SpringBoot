package org.fontory.fontorybe.provide.controller.port;

import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.dto.ProvideCreateDto;

public interface ProvideService {
    Provide getById(Long id);
    Provide create(ProvideCreateDto provideCreateDto);
}
