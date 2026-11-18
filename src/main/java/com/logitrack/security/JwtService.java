package com.logitrack.security;

import com.logitrack.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service @RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    @Value("${app.jwt.minutos}") private long minutos;
    public String crearToken(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet datos = JwtClaimsSet.builder().issuer("logitrack")
            .issuedAt(ahora).expiresAt(ahora.plusSeconds(minutos * 60))
            .subject(usuario.getNombre()).claim("rol", usuario.getRol().name()).build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, datos)).getTokenValue();
    }
    public long segundos() { return minutos * 60; }
}
