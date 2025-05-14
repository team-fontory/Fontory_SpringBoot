package org.fontory.fontorybe.member.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class MemberUpdateRequest {
    private String nickname;
    private Boolean terms;
}
