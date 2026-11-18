package com.logitrack.service;

import com.logitrack.dto.request.MovimientoDtoRequest;
import com.logitrack.dto.response.MovimientoDtoResponse;
import java.time.LocalDateTime;
import java.util.List;
public interface MovimientoService {
    MovimientoDtoResponse guardar(MovimientoDtoRequest dto, String usuario);
    List<MovimientoDtoResponse> listar(LocalDateTime desde, LocalDateTime hasta);
    MovimientoDtoResponse buscarId(Long id);
}
