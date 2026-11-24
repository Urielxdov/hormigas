package com.example.hormigas.security;

import com.example.hormigas.security.domain.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TokenServiceImplTest {

    @Autowired TokenService tokenService;
    @Autowired JwtEncoder jwtEncoder;

    @Value("${app.bootstrap.admin.email}") String adminEmail;
    @Value("${jwt.expiration}") long expirationMs;

    @Test
    void laExpiracionUsaMilisegundos() {
        String token = tokenService.generateToken(new UsernamePasswordAuthenticationToken(adminEmail, null, List.of()));

        assertTrue(tokenService.validateToken(token));
        Duration vida = Duration.between(Instant.now(), tokenService.getExpirationDateFromToken(token).toInstant());
        // jwt.expiration = 86400000 ms -> 24 h (antes se sumaba como minutos: ~164 años)
        assertTrue(vida.compareTo(Duration.ofMillis(expirationMs).plusMinutes(1)) <= 0, "vida " + vida);
        assertTrue(vida.compareTo(Duration.ofMillis(expirationMs).minusMinutes(1)) >= 0, "vida " + vida);
    }

    @Test
    void tokenInvalidoDevuelveFalse() {
        assertFalse(tokenService.validateToken("esto.no.es-un-jwt"));
        assertFalse(tokenService.validateToken(""));
    }

    @Test
    void tokenExpiradoDevuelveFalse() {
        Instant hace = Instant.now().minus(Duration.ofHours(2));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(adminEmail)
                .issuedAt(hace)
                .expiresAt(hace.plus(Duration.ofMinutes(5)))
                .build();
        String expirado = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertFalse(tokenService.validateToken(expirado));
    }
}
