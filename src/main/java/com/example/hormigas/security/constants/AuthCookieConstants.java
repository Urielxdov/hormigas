package com.example.hormigas.security.constants;

public class AuthCookieConstants {
    private AuthCookieConstants() {
        throw new UnsupportedOperationException("Esta clase nunca debe de ser inicializada");
    }
    public static final String TOKEN_COOKIE_NAME = "auth-token";
    public static final boolean HTTP_ONLY = true;
    public static final boolean COOKIE_SECURE = true;
    public static final int COOKIE_MAX_AGE = 60 * 12; // 12 minutos
    public static final String SAME_SITE = "Strict";
}
