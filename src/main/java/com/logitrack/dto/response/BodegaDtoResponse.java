package com.logitrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.logitrack.model.*;
public record BodegaDtoResponse(Long id, String nombre, String ubicacion, Integer capacidad, String encargado) {
    
}
