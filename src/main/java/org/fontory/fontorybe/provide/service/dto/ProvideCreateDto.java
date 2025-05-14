package org.fontory.fontorybe.provide.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

@Getter
@Builder
@AllArgsConstructor
public class ProvideCreateDto {
    private Provider provider;
    private String providedId;
    private String email;
}
