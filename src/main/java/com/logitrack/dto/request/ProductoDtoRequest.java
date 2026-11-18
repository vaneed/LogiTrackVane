package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record ProductoDtoRequest(
    @NotBlank @Size(max = 80) String nombre,
    @NotBlank @Size(max = 60) String categoria,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precio
) {}
