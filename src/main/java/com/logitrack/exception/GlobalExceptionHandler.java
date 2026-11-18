package com.logitrack.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private ResponseEntity<ErrorResponse> error(int estado, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(estado).body(new ErrorResponse(LocalDateTime.now(), estado, mensaje, request.getRequestURI()));
    }
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(Exception e, HttpServletRequest r) { return error(404, e.getMessage(), r); }
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> negocio(Exception e, HttpServletRequest r) { return error(400, e.getMessage(), r); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validar(MethodArgumentNotValidException e, HttpServletRequest r) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(java.util.stream.Collectors.joining("; "));
        return error(400, mensaje, r);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> formato(Exception e, HttpServletRequest r) { return error(400, "Revisa el JSON, los campos y el formato de las fechas.", r); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(Exception e, HttpServletRequest r) { return error(400, "El registro está duplicado o tiene datos relacionados.", r); }
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> concurrencia(Exception e, HttpServletRequest r) { return error(400, "Otro usuario está modificando estos datos. Intenta nuevamente.", r); }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> autenticacion(Exception e, HttpServletRequest r) { return error(401, "Usuario o contraseña incorrectos.", r); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> permiso(Exception e, HttpServletRequest r) { return error(403, "No tienes permiso para esta acción.", r); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> ruta(Exception e, HttpServletRequest r) { return error(404, "Ruta no encontrada.", r); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e, HttpServletRequest r) {
        log.error("Error inesperado", e);
        return error(500, "Ocurrió un error interno. Intenta más tarde.", r);
    }
}
