package com.logitrack.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.logitrack.exception.ErrorResponse;


import com.logitrack.dto.response.ResumenDtoResponse;
import com.logitrack.model.*;
import com.logitrack.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor @Tag(name = "Consultas y reportes")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ConsultaController {
    private final ConsultaService servicio;
    @Operation(summary = "Auditorías por usuario y/o tipo de operación (ADMIN)")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/auditorias")
    public List<Auditoria> auditorias(@RequestParam(required = false) String usuario, @RequestParam(required = false) Operacion operacion) {
        return servicio.auditorias(usuario, operacion);
    }
    @Operation(summary = "Stock total por bodega y productos más movidos", description = "Ordenados por suma de unidades en entradas, salidas y transferencias. Una transferencia se cuenta una vez.")
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @GetMapping("/reportes/resumen")
    public ResumenDtoResponse resumen() { return servicio.resumen(); }
}
