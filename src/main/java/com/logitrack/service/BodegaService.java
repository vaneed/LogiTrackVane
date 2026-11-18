package com.logitrack.service;

import com.logitrack.dto.request.BodegaDtoRequest;
import com.logitrack.dto.response.*;
import java.util.List;
public interface BodegaService {
    BodegaDtoResponse guardar(BodegaDtoRequest dto);
    BodegaDtoResponse actualizar(BodegaDtoRequest dto, Long id);
    void eliminar(Long id);
    List<BodegaDtoResponse> listar();
    BodegaDtoResponse buscarId(Long id);
    List<ExistenciaDtoResponse> existencias(Long id);
}
