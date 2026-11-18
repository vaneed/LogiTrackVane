package com.logitrack.repository;

import com.logitrack.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface ExistenciaRepository extends JpaRepository<Existencia, Long> {
    Optional<Existencia> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);
    List<Existencia> findByBodegaId(Long bodegaId);
    boolean existsByBodegaId(Long id);
    boolean existsByProductoId(Long id);
}
