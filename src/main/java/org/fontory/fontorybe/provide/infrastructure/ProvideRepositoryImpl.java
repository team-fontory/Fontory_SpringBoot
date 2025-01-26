package org.fontory.fontorybe.provide.infrastructure;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProvideRepositoryImpl implements ProvideRepository {
    private final ProvideJpaRepository provideJpaRepository;

    @Override
    public Optional<Provide> findById(Long id) {
        return provideJpaRepository.findById(id).map(ProvideEntity::toModel);
    }

    @Override
    public Provide save(Provide provide) {
        return provideJpaRepository.save(ProvideEntity.from(provide)).toModel();
    }
}
