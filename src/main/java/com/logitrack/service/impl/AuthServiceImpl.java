package com.logitrack.service.impl;

import com.logitrack.dto.request.*;
import com.logitrack.dto.response.LoginDtoResponse;
import com.logitrack.exception.ReglaNegocioException;
import com.logitrack.model.*;
import com.logitrack.repository.UsuarioRepository;
import com.logitrack.security.JwtService;
import com.logitrack.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    @Override @Transactional
    public LoginDtoResponse registrar(RegistroDtoRequest dto) {
        if (usuarioRepository.existsByNombre(dto.nombre())) throw new ReglaNegocioException("Ese usuario ya existe.");
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setClave(encoder.encode(dto.clave()));
        usuario.setRol(Rol.EMPLEADO); // Un registro público nunca puede darse permisos ADMIN.
        usuarioRepository.save(usuario);
        return respuesta(usuario);
    }
    @Override
    public LoginDtoResponse login(LoginDtoRequest dto) {
        Usuario usuario = usuarioRepository.findByNombre(dto.nombre())
            .orElseThrow(() -> new BadCredentialsException("Datos incorrectos"));
        if (!encoder.matches(dto.clave(), usuario.getClave())) throw new BadCredentialsException("Datos incorrectos");
        return respuesta(usuario);
    }
    private LoginDtoResponse respuesta(Usuario usuario) {
        return new LoginDtoResponse(jwt.crearToken(usuario), "Bearer", jwt.segundos(), usuario.getNombre(), usuario.getRol());
    }
}
