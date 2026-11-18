package com.logitrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.logitrack.model.*;
public record ResumenDtoResponse(List<StockBodega> stockPorBodega, List<ProductoMovido> productosMasMovidos) {
    public record StockBodega(Long bodegaId, String nombre, long stockTotal) {}
    public record ProductoMovido(Long productoId, String nombre, long cantidadMovida) {}
}
