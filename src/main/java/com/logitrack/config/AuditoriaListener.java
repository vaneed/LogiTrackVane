package com.logitrack.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logitrack.model.RegistroAuditable;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Hibernate obtiene este listener de Spring. No guardamos Auditoria con JPA
// dentro de un callback para evitar otro flush. JDBC usa la misma transacción.
@Component
@RequiredArgsConstructor
public class AuditoriaListener {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    @PostLoad
    public void cargar(RegistroAuditable dato) { dato.setValoresAnteriores(json(dato)); }
    @PostPersist
    public void insertar(RegistroAuditable dato) {
        registrar("INSERT", dato, null, json(dato));
        cargar(dato);
    }
    @PostUpdate
    public void actualizar(RegistroAuditable dato) {
        registrar("UPDATE", dato, dato.getValoresAnteriores(), json(dato));
        cargar(dato);
    }
    @PreRemove
    public void eliminar(RegistroAuditable dato) { registrar("DELETE", dato, json(dato), null); }

    private String json(RegistroAuditable dato) {
        try { return mapper.writeValueAsString(dato); }
        catch (JsonProcessingException e) { throw new IllegalStateException("No se pudo auditar el cambio", e); }
    }
    private void registrar(String operacion, Object dato, String anterior, String nuevo) {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        String usuario = autenticacion == null ? "SISTEMA" : autenticacion.getName();
        jdbc.update("INSERT INTO auditoria (operacion, fecha, usuario, entidad, valores_anteriores, valores_nuevos) VALUES (?, ?, ?, ?, ?, ?)",
            operacion, LocalDateTime.now(ZoneOffset.UTC), usuario, dato.getClass().getSimpleName(), anterior, nuevo);
    }
}
