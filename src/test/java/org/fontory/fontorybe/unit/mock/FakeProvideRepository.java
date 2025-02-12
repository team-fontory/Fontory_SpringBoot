package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.service.port.ProvideRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeProvideRepository implements ProvideRepository {
    private final AtomicLong authGeneratedID = new AtomicLong(0);
    private final List<Provide> data = new ArrayList<>();


    @Override
    public Provide save(Provide provide) {
        LocalDateTime now = LocalDateTime.now();
        if (provide.getId() == null || provide.getId() == 0) {
            Provide newProvide = Provide.builder()
                    .id(authGeneratedID.incrementAndGet())
                    .providedId(provide.getProvidedId())
                    .provider(provide.getProvider())
                    .email(provide.getEmail())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            data.add(newProvide);
            return newProvide;
        } else {
            data.removeIf(p -> p.getId().equals(provide.getId()));
            Provide newProvide = Provide.builder()
                    .id(provide.getId())
                    .providedId(provide.getProvidedId())
                    .provider(provide.getProvider())
                    .email(provide.getEmail())
                    .memberId(provide.getMemberId())
                    .createdAt(provide.getCreatedAt())
                    .updatedAt(now)
                    .build();
            data.add(newProvide);
            return newProvide;
        }
    }


    @Override
    public Optional<Provide> findById(Long id) {
        return data.stream()
                .filter(item -> item.getId().equals(id))
                .findAny();
    }
}
