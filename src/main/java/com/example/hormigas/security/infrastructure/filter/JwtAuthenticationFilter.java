package com.example.hormigas.security.infrastructure.filter;

import com.example.hormigas.security.application.AuthCookieConstants;
import com.example.hormigas.security.domain.services.AuthService;
import com.example.hormigas.security.infrastructure.config.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AuthService authService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            AuthService authService,
            UserDetailsService userDetailsService
    ) {
        this.authService = authService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // IMPORTANTE: no solo login, también evita problemas con variantes
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("[JWT FILTER] Path: {}", path);

        Optional<String> token = getJwtFromHeader(request);

        if (token.isEmpty()) {
            token = getJwtFromCookie(request);
        }

        if (token.isEmpty()) {
            logger.info("[JWT FILTER] No token found, continuing chain");
            filterChain.doFilter(request, response);
            return;
        }

        logger.info("[JWT FILTER] Token found");

        // VALIDACIÓN
        if (!authService.validateToken(token.get())) {
            logger.warn("[JWT FILTER] Invalid token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        // AUTENTICACIÓN
        String username = authService.getUserFromToken(token.get());
        logger.info("[JWT FILTER] Auth user: {}", username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.info("[JWT FILTER] Authentication set, continuing chain");

        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || ObjectUtils.isEmpty(cookies)) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(c -> AuthCookieConstants.TOKEN_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private Optional<String> getJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }

        return Optional.empty();
    }
}