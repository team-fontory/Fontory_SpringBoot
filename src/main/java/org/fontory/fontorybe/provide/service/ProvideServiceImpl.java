package org.fontory.fontorybe.provide.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Builder
@RequiredArgsConstructor
public class ProvideServiceImpl implements ProvideService {
    private final ProvideRepository provideRepository;

    @Override
    public Provide getOrThrownById(Long id) {
        return provideRepository.findById(id)
                .orElseThrow(ProvideNotFoundException::new);
    }

    @Override
    @Transactional
    public Provide create(ProvideCreateDto provideCreateDto) {
        Provide provide = Provide.from(provideCreateDto);
        return provideRepository.save(provide);
    }

    // Not for Service
    // Should be deleted
    @Override
    @Transactional
    public Long getTempProvideId() {
        return create(
                ProvideCreateDto.builder()
                                .provider(Provider.GOOGLE)
                                .providedId(UUID.randomUUID().toString())
                                .email("tempEmail")
                                .build()).getId();
    }
}
