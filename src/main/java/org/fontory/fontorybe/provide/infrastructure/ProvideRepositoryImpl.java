package org.fontory.fontorybe.provide.infrastructure;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProvideRepositoryImpl implements ProvideRepository {
    private final ProvideJpaRepository provideJpaRepository;
    private final EntityManager em;

    @Override
    public Optional<Provide> findById(Long id) {
        return provideJpaRepository.findById(id)
                .map(ProvideEntity::toModel);
    }

    @Override
    public Optional<Provide> findByOAuthInfo(String userIdentifier, Provider provider) {
        return provideJpaRepository.findByProvidedIdAndProvider(userIdentifier, provider)
                .map(ProvideEntity::toModel);
    }

    @Override
    public Provide save(Provide provide) {
        ProvideEntity savedEntity = provideJpaRepository.save(ProvideEntity.from(provide));

        em.flush();
        em.refresh(savedEntity);

        return savedEntity.toModel();
    }
}
