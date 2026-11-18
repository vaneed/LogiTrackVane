package com.logitrack.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.logitrack.exception.ErrorResponse;


import com.logitrack.dto.request.ProductoDtoRequest;
import com.logitrack.dto.response.*;
import com.logitrack.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/productos") @RequiredArgsConstructor
@Tag(name = "Productos")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ProductoController {
    private final ProductoService servicio;
    @Operation(summary = "Registrar producto (ADMIN)")
    @ApiResponse(responseCode = "201", description = "Registro creado", useReturnTypeSchema = true)
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoDtoResponse> guardar(@Valid @RequestBody ProductoDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(dto));
    }
    @Operation(summary = "Listar productos")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping
    public List<ProductoDtoResponse> listar() { return servicio.listar(); }
    @Operation(summary = "Buscar producto por ID")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/{id}")
    public ProductoDtoResponse buscar(@PathVariable Long id) { return servicio.buscarId(id); }
    @Operation(summary = "Actualizar producto (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ProductoDtoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDtoRequest dto) { return servicio.actualizar(dto, id); }
    @Operation(summary = "Eliminar producto sin historial (ADMIN)")
    @ApiResponse(responseCode = "204", description = "Registro eliminado", content = @Content)
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { servicio.eliminar(id); return ResponseEntity.noContent().build(); }
    @Operation(summary = "Consultar productos con stock menor a 10")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/stock-bajo")
    public List<ProductoDtoResponse> stockBajo() { return servicio.stockBajo(); }
}
