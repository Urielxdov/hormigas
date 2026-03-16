package com.example.hormigas.security.service;

import com.example.hormigas.security.entity.Usuario;
import com.example.hormigas.security.service.interfaces.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class TokenServiceImpl implements TokenService {
    private final static Logger logger = LogManager.getLogger(TokenServiceImpl.class);
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public TokenServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
    }


    @Override
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Usuario currentUser = (Usuario) authentication.getPrincipal();
        // Representa los claims del token
        JwtClaimsSet claims = JwtClaimsSet.builder() // Permite añadir los claims
                .subject(currentUser.getCorreo()) // Los claims guardan info
                .issuedAt(now) // Aqui se guarda el correo y la fecha de expiracion
                .expiresAt(now.plus(expiration, ChronoUnit.MINUTES))
                .build();
        // Reperesenta los parametros del token
        // Se guarda el algoritmo de firma y los claims
        var jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    @Override
    public String getUserFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception exception) {
            logger.error("[USER] : Weeoe al tratar de validar el token", exception);
            throw new BadJwtException("Error mientras se trataba de validar el token");
        }
    }
}
