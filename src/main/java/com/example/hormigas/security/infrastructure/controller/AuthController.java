package com.example.hormigas.security.infrastructure.controller;

import com.example.hormigas.security.application.AuthCookieConstants;
import com.example.hormigas.security.infrastructure.dtos.CreateUsuarioDTO;
import com.example.hormigas.security.infrastructure.dtos.LoginRequestDTO;
import com.example.hormigas.security.domain.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public void createUser(@RequestBody CreateUsuarioDTO createUsuarioDTO) {
        authService.createUser(createUsuarioDTO);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        final Cookie cookie = new Cookie(AuthCookieConstants.TOKEN_COOKIE_NAME, "");
        cookie.setMaxAge(0);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        final String token = authService.login(loginRequestDTO);
        final Cookie cookie = createAuthCookie(token);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);

        return Map.of("token", token);
    }

    private Cookie createAuthCookie(String token) {
        final String SAME_SITE_KEY = "SameSite";
        final Cookie cookie = new Cookie(AuthCookieConstants.TOKEN_COOKIE_NAME, token);
        cookie.setHttpOnly(AuthCookieConstants.HTTP_ONLY);
        cookie.setSecure(AuthCookieConstants.COOKIE_SECURE);
        cookie.setMaxAge(AuthCookieConstants.COOKIE_MAX_AGE);
        cookie.setAttribute(SAME_SITE_KEY, AuthCookieConstants.SAME_SITE);
        return cookie;
    }
}
