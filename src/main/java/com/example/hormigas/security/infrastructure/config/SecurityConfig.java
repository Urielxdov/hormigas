package com.example.hormigas.security.infrastructure.config;

import com.example.hormigas.security.domain.Role;
import com.example.hormigas.security.infrastructure.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public static final String LOGIN_URL_MATCHER = "/api/auth/login";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

   // SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS configurado
            .csrf(csrf -> csrf.disable()) // CSRF desactivado para APIs
            
            // VITAL para JWT: No guardar estado de sesión
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
            )
            
            .authorizeHttpRequests(auth -> auth
                // 1. Permitir Login
                .requestMatchers(LOGIN_URL_MATCHER).permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // 2. Reglas específicas (SIEMPRE ARRIBA)
                .requestMatchers("/api/empresa/create").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers("/api/empresa/delete/**").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers("/api/empresa/all").hasAuthority("ROLE_SUPER_ADMIN")

                // 3. Regla genérica (SIEMPRE AL FINAL de su grupo)
                .requestMatchers("/api/empresa/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                .requestMatchers("/api/usuario/list").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                .requestMatchers("/api/usuario/create").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                .requestMatchers("/api/usuario/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                .requestMatchers("/api/sucursal/**").hasAuthority("ROLE_ADMIN")

                // 4. Cualquier otra cosa logueado
                .anyRequest().authenticated()
            )
            
            // Tu filtro JWT antes del de usuario/password
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String roles = jwt.getClaimAsString("roles");
            if (roles == null || roles.isBlank()) return List.of();
            
            return Arrays.stream(roles.split(" "))
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // Fuerza el prefijo
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

