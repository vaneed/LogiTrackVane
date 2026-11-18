package com.logitrack.mapper;

import com.logitrack.model.Bodega;
import com.logitrack.dto.request.BodegaDtoRequest;
import com.logitrack.dto.response.BodegaDtoResponse;
import org.springframework.stereotype.Component;
@Component
public class BodegaMapper {
    public BodegaDtoResponse entityToDto(Bodega dato) {
        return new BodegaDtoResponse(dato.getId(), dato.getNombre(), dato.getUbicacion(), dato.getCapacidad(), dato.getEncargado());
    }
    public Bodega dtoToEntity(BodegaDtoRequest dto) {
        Bodega dato = new Bodega();
        updateEntityFromDto(dato, dto);
        return dato;
    }
    public void updateEntityFromDto(Bodega dato, BodegaDtoRequest dto) {
        dato.setNombre(dto.nombre());
dato.setUbicacion(dto.ubicacion());
dato.setCapacidad(dto.capacidad());
dato.setEncargado(dto.encargado());
    }
}
