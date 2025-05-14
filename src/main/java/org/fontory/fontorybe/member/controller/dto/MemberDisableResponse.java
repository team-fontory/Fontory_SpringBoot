package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberDisableResponse {
    private final Long memberId;
    private final LocalDateTime deletedAt;

    public static MemberDisableResponse from(Member member) {
        return MemberDisableResponse.builder()
                .memberId(member.getId())
                .deletedAt(member.getDeletedAt())
                .build();
    }
}
