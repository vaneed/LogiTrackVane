package com.logitrack.repository;

import com.logitrack.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface DetalleMovimientoRepository extends JpaRepository<DetalleMovimiento, Long> {
    List<DetalleMovimiento> findByMovimientoId(Long id);
    boolean existsByProductoId(Long id);
    @Query("select d.productoId as productoId, sum(d.cantidad) as cantidad from DetalleMovimiento d group by d.productoId order by sum(d.cantidad) desc, d.productoId asc")
    List<ProductoMovido> productosMasMovidos();
    interface ProductoMovido {
        Long getProductoId();
        Long getCantidad();
    }
}
