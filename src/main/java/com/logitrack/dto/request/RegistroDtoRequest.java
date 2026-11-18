package com.logitrack.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.math.BigDecimal;
import com.logitrack.model.TipoMovimiento;
public record RegistroDtoRequest(
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]{3,50}", message = "Usa entre 3 y 50 letras, números o guion bajo") String nombre,
    @NotBlank @Size(min = 8, max = 60) String clave
) {}
