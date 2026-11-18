package com.logitrack.service;

import com.logitrack.dto.request.ProductoDtoRequest;
import com.logitrack.dto.response.*;
import java.util.List;
public interface ProductoService {
    ProductoDtoResponse guardar(ProductoDtoRequest dto);
    ProductoDtoResponse actualizar(ProductoDtoRequest dto, Long id);
    void eliminar(Long id);
    List<ProductoDtoResponse> listar();
    ProductoDtoResponse buscarId(Long id);
    List<ProductoDtoResponse> stockBajo();
}
