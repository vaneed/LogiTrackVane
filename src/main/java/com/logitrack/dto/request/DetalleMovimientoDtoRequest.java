package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record DetalleMovimientoDtoRequest(
    @NotNull @Min(1) Long productoId,
    @NotNull @Min(1) @Max(1000000) Integer cantidad
) {}
