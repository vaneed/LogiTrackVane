package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record BodegaDtoRequest(
    @NotBlank @Size(max = 80) String nombre,
    @NotBlank @Size(max = 150) String ubicacion,
    @NotNull @Min(1) Integer capacidad,
    @NotBlank @Size(max = 80) String encargado
) {}
