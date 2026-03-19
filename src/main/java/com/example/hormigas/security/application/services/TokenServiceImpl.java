package com.example.hormigas.security.application.services;

import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.domain.Usuario;
import com.example.hormigas.security.domain.Rol;
import com.example.hormigas.security.domain.repository.UsuarioRepository;
import com.example.hormigas.security.domain.services.TokenService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

    private final UsuarioRepository usuarioRepository;

    public TokenServiceImpl(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UsuarioRepository usuarioRepository) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
        this.usuarioRepository = usuarioRepository;
    }


    @Override
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        // Obtienes el username (correo) desde Spring Security UserDetails
        String correo = authentication.getName(); // equivalente a authentication.getPrincipal().getUsername()

        // Si necesitas info adicional de la entidad Usuario, la buscas en el repo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

//        String scope = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(" "));

        String roles = usuario.getRoles().stream()
                .map(Role::name)
                .map(rol -> rol.startsWith("ROLE_") ? rol : "ROLE_" + rol)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plus(expiration, ChronoUnit.MINUTES))
                .claim("roles", roles)
                .claim("id", usuario.getId())        // ejemplo: info extra
                .claim("empresaId", usuario.getEmpresa().getId()) // info de empresa
                .build();

        var jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return this.jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }
    @Override
    public String getUserFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        logger.info("[USER] : email from user {}", jwtToken.getSubject());
        return jwtToken.getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception exception) {
            logger.error("[USER] : Error al tratar de validar el token", exception);
            throw new BadJwtException("Error mientras se trataba de validar el token");
        }
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        Instant expInstant = jwt.getExpiresAt();
        return Date.from(expInstant);
    }

    @Override
    public boolean isTokemExpired(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getExpiresAt().isBefore(Instant.now());
    }
}
