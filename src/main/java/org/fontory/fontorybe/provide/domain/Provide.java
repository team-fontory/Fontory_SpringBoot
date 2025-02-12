package org.fontory.fontorybe.provide.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Provide {
    private Long id;

    private Provider provider;

    private String providedId;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long memberId;

    public static Provide from(ProvideCreateDto provideCreateDto) {
        return Provide.builder()
                .provider(provideCreateDto.getProvider())
                .providedId(provideCreateDto.getProvidedId())
                .email(provideCreateDto.getEmail())
                .build();
    }
}
