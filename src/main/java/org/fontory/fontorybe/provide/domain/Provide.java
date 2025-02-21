package org.fontory.fontorybe.provide.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

@ToString
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Provide implements Serializable {
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

    public static Provide from(Provider provider, String provideId, String email) {
        return Provide.builder()
                .provider(provider)
                .providedId(provideId)
                .email(email)
                .build();
    }

    public void setMember(Long memberId) {
        this.memberId = memberId;
    }
}
