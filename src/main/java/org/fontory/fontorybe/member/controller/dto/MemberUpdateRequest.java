package org.fontory.fontorybe.member.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MemberUpdateRequest {
    private String nickname;
    private String profileImageKey;
    private Boolean terms;
}
