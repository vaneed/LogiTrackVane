package com.logitrack.repository;

import com.logitrack.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);
    List<Movimiento> findAllByOrderByFechaDesc();
    boolean existsByBodegaOrigenIdOrBodegaDestinoId(Long origen, Long destino);
}
