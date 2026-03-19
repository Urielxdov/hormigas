package com.example.hormigas.security.infrastructure.filter;

import com.example.hormigas.security.infrastructure.config.SecurityConfig;
import com.example.hormigas.security.application.AuthCookieConstants;
import com.example.hormigas.security.domain.services.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
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
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        final String requestURI = request.getRequestURI();
        return requestURI.equals(SecurityConfig.LOGIN_URL_MATCHER);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = getJwtFromHeader(request);

        if(token.isEmpty()) {
            token = getJwtFromCookie(request);
        }

        if(token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!authService.validateToken(token.get())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BadCredentialsException("Token invalido");
        }

        String userName = authService.getUserFromToken(token.get());
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private Optional<String> getJwtFromCookie(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null || ObjectUtils.isEmpty(cookies)) {
            return Optional.empty();
        }
        return (Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(AuthCookieConstants.TOKEN_COOKIE_NAME))
                .map(Cookie::getValue))
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

