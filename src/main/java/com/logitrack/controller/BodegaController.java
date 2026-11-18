package com.logitrack.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.logitrack.exception.ErrorResponse;


import com.logitrack.dto.request.BodegaDtoRequest;
import com.logitrack.dto.response.*;
import com.logitrack.service.BodegaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/bodegas") @RequiredArgsConstructor
@Tag(name = "Bodegas")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class BodegaController {
    private final BodegaService servicio;
    @Operation(summary = "Registrar bodega (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Registro creado", useReturnTypeSchema = true)
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BodegaDtoResponse> guardar(@Valid @RequestBody BodegaDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(dto));
    }
    @Operation(summary = "Listar bodegas")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping
    public List<BodegaDtoResponse> listar() { return servicio.listar(); }
    @Operation(summary = "Buscar bodega por ID")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/{id}")
    public BodegaDtoResponse buscar(@PathVariable Long id) { return servicio.buscarId(id); }
    @Operation(summary = "Actualizar bodega (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public BodegaDtoResponse actualizar(@PathVariable Long id, @Valid @RequestBody BodegaDtoRequest dto) { return servicio.actualizar(dto, id); }
    @Operation(summary = "Eliminar bodega sin historial (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Registro eliminado", content = @Content)
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { servicio.eliminar(id); return ResponseEntity.noContent().build(); }
    @Operation(summary = "Consultar existencias de una bodega")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/{id}/existencias")
    public List<ExistenciaDtoResponse> existencias(@PathVariable Long id) { return servicio.existencias(id); }
}
