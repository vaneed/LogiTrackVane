package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record MovimientoDtoRequest(
    @NotNull TipoMovimiento tipo,
    @Min(1) Long bodegaOrigenId,
    @Min(1) Long bodegaDestinoId,
    @NotEmpty @Size(max = 100) List<@NotNull @Valid DetalleMovimientoDtoRequest> productos
) {}
