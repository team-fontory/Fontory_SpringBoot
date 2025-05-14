package org.fontory.fontorybe.provide.controller.port;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;

public interface ProvideService {
    Provide getOrThrownById(Long id);
    Provide create(ProvideCreateDto provideCreateDto);
    Provide create(Provider provider, String email, String providedId);
    Provide setMember(Provide provide, Member member);
}
