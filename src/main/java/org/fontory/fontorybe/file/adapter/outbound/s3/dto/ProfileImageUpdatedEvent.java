package org.fontory.fontorybe.file.adapter.outbound.s3.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProfileImageUpdatedEvent {
    private final Long memberId;
    private final String tempKey;
    private final String fixedKey;
}
