package com.logitrack.mapper;

import com.logitrack.model.Producto;
import com.logitrack.dto.request.ProductoDtoRequest;
import com.logitrack.dto.response.ProductoDtoResponse;
import org.springframework.stereotype.Component;
@Component
public class ProductoMapper {
    public ProductoDtoResponse entityToDto(Producto dato) {
        return new ProductoDtoResponse(dato.getId(), dato.getNombre(), dato.getCategoria(), dato.getStock(), dato.getPrecio());
    }
    public Producto dtoToEntity(ProductoDtoRequest dto) {
        Producto dato = new Producto();
        updateEntityFromDto(dato, dto);
        return dato;
    }
    public void updateEntityFromDto(Producto dato, ProductoDtoRequest dto) {
        dato.setNombre(dto.nombre());
dato.setCategoria(dto.categoria());
dato.setPrecio(dto.precio());
    }
}
