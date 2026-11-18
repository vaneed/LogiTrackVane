package com.logitrack.exception;

import java.time.LocalDateTime;
public record ErrorResponse(LocalDateTime fecha, int estado, String mensaje, String ruta) {}
