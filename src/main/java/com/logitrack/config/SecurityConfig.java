package com.logitrack.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.exception.ErrorResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecretKey claveJwt(@Value("${app.jwt.secret}") String clave) {
        byte[] bytes = clave.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
    @Bean public JwtEncoder jwtEncoder(SecretKey clave) { return new NimbusJwtEncoder(new ImmutableSecret<>(clave)); }
    @Bean public JwtDecoder jwtDecoder(SecretKey clave) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("logitrack"));
        return decoder;
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http, ObjectMapper mapper) throws Exception {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertir = new JwtAuthenticationConverter();
        convertir.setJwtGrantedAuthoritiesConverter(roles);
        var entrada = (org.springframework.security.web.AuthenticationEntryPoint) (req, res, e) -> {
            res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(res.getOutputStream(), new ErrorResponse(LocalDateTime.now(), 401, "Inicia sesión o renueva tu token.", req.getRequestURI()));
        };
        var denegado = (org.springframework.security.web.access.AccessDeniedHandler) (req, res, e) -> {
            res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
            mapper.writeValue(res.getOutputStream(), new ErrorResponse(LocalDateTime.now(), 403, "No tienes permiso para esta acción.", req.getRequestURI()));
        };
        http.csrf(csrf -> csrf.disable()) // Usamos JWT en Authorization, no cookies de sesión.
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                .requestMatchers("/auditorias/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint(entrada).accessDeniedHandler(denegado))
            .oauth2ResourceServer(o -> o.authenticationEntryPoint(entrada).accessDeniedHandler(denegado)
                .jwt(j -> j.jwtAuthenticationConverter(convertir)));
        return http.build();
    }
}
