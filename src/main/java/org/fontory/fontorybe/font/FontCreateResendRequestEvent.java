package org.fontory.fontorybe.font;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@RequiredArgsConstructor
public class FontCreateResendRequestEvent {
    private final Font font;
    private final String phoneNumber;
}
