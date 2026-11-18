package com.logitrack.repository;

import com.logitrack.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    @Query("select a from Auditoria a where (:usuario is null or a.usuario = :usuario) and (:operacion is null or a.operacion = :operacion) order by a.fecha desc, a.id desc")
    List<Auditoria> filtrar(String usuario, Operacion operacion);
}
