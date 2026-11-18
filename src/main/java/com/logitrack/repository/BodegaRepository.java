package com.logitrack.repository;

import com.logitrack.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface BodegaRepository extends JpaRepository<Bodega, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bodega b order by b.id")
    List<Bodega> bloquearTodas();
}
