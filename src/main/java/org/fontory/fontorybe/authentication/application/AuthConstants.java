package org.fontory.fontorybe.authentication.application;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthConstants {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public static final String ACCESS_TOKEN_COOKIE_SAME_SITE = "Lax";
    public static final String REFRESH_TOKEN_COOKIE_SAME_SITE = "Strict";

    public static final boolean SECURE = true;
    public static final boolean HTTP_ONLY = true;
    public static final String PATH = "/";
}
