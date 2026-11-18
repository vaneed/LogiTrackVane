package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record LoginDtoRequest(
    @NotBlank @Size(max = 50) String nombre,
    @NotBlank @Size(max = 60) String clave
) {}
