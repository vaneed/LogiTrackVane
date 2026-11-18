package com.logitrack.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.logitrack.exception.ErrorResponse;


import com.logitrack.dto.request.*;
import com.logitrack.dto.response.LoginDtoResponse;
import com.logitrack.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth") @RequiredArgsConstructor @Tag(name = "Autenticación")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Datos inválidos o regla de negocio", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = "Autenticación requerida o token inválido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Error interno", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AuthController {
    private final AuthService servicio;
    @Operation(summary = "Iniciar sesión y obtener JWT", security = {})
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @ApiResponse(responseCode = "200", description = "Operación correcta", useReturnTypeSchema = true)
    @PostMapping("/login")
    public LoginDtoResponse login(@Valid @RequestBody LoginDtoRequest dto) { return servicio.login(dto); }
    @Operation(summary = "Registrar empleado y obtener JWT", security = {})
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @ApiResponse(responseCode = "201", description = "Empleado registrado", useReturnTypeSchema = true)
    @PostMapping("/register")
    public ResponseEntity<LoginDtoResponse> registrar(@Valid @RequestBody RegistroDtoRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(dto));
    }
}
