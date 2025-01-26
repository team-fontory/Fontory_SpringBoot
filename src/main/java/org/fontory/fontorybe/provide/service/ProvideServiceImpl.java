package org.fontory.fontorybe.provide.service;

import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvideServiceImpl implements ProvideService {
    private final ProvideRepository provideRepository;

    @Override
    public Provide getById(Long id) {
        return provideRepository.findById(id)
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Provide create(ProvideCreateDto provideCreateDto) {
        Provide provide = Provide.from(provideCreateDto);
        return provideRepository.save(provide);
    }
}
