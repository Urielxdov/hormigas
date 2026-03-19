package com.example.hormigas.security.domain.services;

import org.springframework.security.core.Authentication;

import java.util.Date;

public interface TokenService {
    String generateToken(Authentication authentication);
    String getUserFromToken(String token);
    boolean validateToken(String token);
    public boolean isTokemExpired(String token);
    public Date getExpirationDateFromToken(String token);
}
