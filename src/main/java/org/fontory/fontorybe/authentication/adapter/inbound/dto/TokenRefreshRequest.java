package org.fontory.fontorybe.authentication.adapter.inbound.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequest {
    private String refreshToken;
}
