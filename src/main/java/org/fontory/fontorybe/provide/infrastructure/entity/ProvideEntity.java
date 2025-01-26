package org.fontory.fontorybe.provide.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.common.domain.BaseEntity;
import org.fontory.fontorybe.provide.domain.Provide;

@Entity
@Getter
@Table(name = "provide")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProvideEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provide_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providedId;

    private String email;

    private Long memberId;

    public Provide toModel() {
        return Provide.builder()
                .id(id)
                .provider(provider)
                .providedId(providedId)
                .email(email)
                .memberId(memberId)
                .build();
    }

    public static ProvideEntity from(Provide provide) {
        return ProvideEntity.builder()
                .id(provide.getId())
                .provider(provide.getProvider())
                .providedId(provide.getProvidedId())
                .email(provide.getEmail())
                .memberId(provide.getMemberId())
                .build();
    }
}
