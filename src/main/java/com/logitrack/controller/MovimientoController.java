package com.logitrack.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.logitrack.exception.ErrorResponse;


import com.logitrack.dto.request.MovimientoDtoRequest;
import com.logitrack.dto.response.MovimientoDtoResponse;
import com.logitrack.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
@RestController @RequestMapping("/movimientos") @RequiredArgsConstructor @Tag(name = "Movimientos")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class MovimientoController {
    private final MovimientoService servicio;
    @Operation(summary = "Registrar entrada, salida o transferencia", description = "Usuario y fecha se asignan automáticamente. Entrada: solo destino. Salida: solo origen. Transferencia: ambos, diferentes.")
    @ApiResponse(responseCode = "201", description = "Registro creado", useReturnTypeSchema = true)
    @PostMapping
    public ResponseEntity<MovimientoDtoResponse> guardar(@Valid @RequestBody MovimientoDtoRequest dto, Principal usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(dto, usuario.getName()));
    }
    @Operation(summary = "Listar movimientos o filtrar por rango inclusivo de fechas UTC", description = "Envía ambos parámetros o ninguno. Ejemplo: 2026-01-01T00:00:00.")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping
    public List<MovimientoDtoResponse> listar(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return servicio.listar(desde, hasta);
    }
    @Operation(summary = "Buscar movimiento por ID")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/{id}")
    public MovimientoDtoResponse buscar(@PathVariable Long id) { return servicio.buscarId(id); }
}
