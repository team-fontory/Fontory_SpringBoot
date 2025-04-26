package org.fontory.fontorybe.file.adapter.outboud.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProfileImageUpdatedEvent {
    private final String tempKey;
    private final String fixedKey;
}
