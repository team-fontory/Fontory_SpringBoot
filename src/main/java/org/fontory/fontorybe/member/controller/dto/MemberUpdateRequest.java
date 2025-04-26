package org.fontory.fontorybe.member.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberUpdateRequest {
    private String nickname;
    private String profileImageKey;
    private Boolean terms;
}
