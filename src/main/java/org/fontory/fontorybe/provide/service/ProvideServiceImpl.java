package org.fontory.fontorybe.provide.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.domain.exception.ProvideNotFoundException;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public Provide create(Provider provider, String email, String providedId) {
        Provide provide = Provide.from(provider, providedId, email);
        return provideRepository.save(provide);
    }

    @Override
    @Transactional
    public Provide setMember(Provide provide, Member member) {
        provide.setMember(member.getId());
        return provideRepository.save(provide);
    }
}
