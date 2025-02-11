package org.fontory.fontorybe.provide.controller.port;

import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;

public interface ProvideService {
    Provide getOrThrownById(Long id);
    Provide create(ProvideCreateDto provideCreateDto);
    Long getTempProvideId();
}
