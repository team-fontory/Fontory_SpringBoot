package org.fontory.fontorybe.unit.provide;

import org.fontory.fontorybe.provide.controller.port.ProvideService;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ProvideServiceTest {
    private ProvideService provideService;

    /**
     * TestValues
     */
    Long nonExistentId = -1L;
    String testProvidedId = UUID.randomUUID().toString();
    String testEmail = "testEmail";
    Provider testProvider = Provider.GOOGLE;
    ProvideCreateDto provideCreateDto = new ProvideCreateDto(testProvider, testProvidedId, testEmail);

    @BeforeEach
    void init() {
        TestContainer testContainer = new TestContainer();
        provideService = testContainer.provideService;
    }

    @Test
    @DisplayName("provide - create success test")
    void provideCreateTest() {
        Provide createdProvide = provideService.create(provideCreateDto);

        assertAll(
                () -> assertThat(createdProvide.getId()).isNotNull(),
                () -> assertThat(createdProvide.getProvider()).isEqualTo(testProvider),
                () -> assertThat(createdProvide.getProvidedId()).isEqualTo(testProvidedId),
                () -> assertThat(createdProvide.getEmail()).isEqualTo(testEmail),
                () -> assertThat(createdProvide.getMemberId()).isNull(),
                () -> assertThat(createdProvide.getCreatedAt()).isNotNull(),
                () -> assertThat(createdProvide.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("provide - getOrThrownById fail test caused by not found")
    void provideGetOrThrownByIdTestX() {
        assertThatThrownBy(
                () -> provideService.getOrThrownById(nonExistentId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("provide - getOrThrownById success test")
    void provideGetByIdTest() {
        Provide createdProvide = provideService.create(provideCreateDto);
        Provide foundProvide = provideService.getOrThrownById(createdProvide.getId());

        assertAll(
                () -> assertThat(foundProvide.getId()).isEqualTo(createdProvide.getId()),
                () -> assertThat(foundProvide.getProvider()).isEqualTo(createdProvide.getProvider()),
                () -> assertThat(foundProvide.getProvidedId()).isEqualTo(createdProvide.getProvidedId()),
                () -> assertThat(foundProvide.getEmail()).isEqualTo(createdProvide.getEmail()),
                () -> assertThat(foundProvide.getMemberId()).isEqualTo(createdProvide.getMemberId()),
                () -> assertThat(createdProvide.getCreatedAt()).isNotNull(),
                () -> assertThat(createdProvide.getUpdatedAt()).isNotNull()
        );
    }
}
