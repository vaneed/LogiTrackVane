package com.logitrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.logitrack.model.*;
public record MovimientoDtoResponse(Long id, LocalDateTime fecha, TipoMovimiento tipo, String usuario, Long bodegaOrigenId, Long bodegaDestinoId, List<DetalleMovimientoDtoResponse> productos) {
    
}
