package org.fontory.fontorybe.member.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.fontory.fontorybe.common.domain.BaseEntity;
import org.fontory.fontorybe.member.domain.Member;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birth;

    private String profileImageKey;

    private Long provideId;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private LocalDateTime deletedAt;

    public Member toModel() {
        return Member.builder()
                .id(id)
                .nickname(nickname)
                .gender(gender)
                .birth(birth)
                .profileImageKey(profileImageKey)
                .provideId(provideId)
                .status(status)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .deletedAt(getDeletedAt())
                .build();
    }

    public static MemberEntity from(Member member) {
        return MemberEntity.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .gender(member.getGender())
                .birth(member.getBirth())
                .profileImageKey(member.getProfileImageKey())
                .provideId(member.getProvideId())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .deletedAt(member.getDeletedAt())
                .build();
    }
}
