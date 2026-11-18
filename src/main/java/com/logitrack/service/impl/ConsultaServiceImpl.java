package com.logitrack.service.impl;

import com.logitrack.dto.response.ResumenDtoResponse;
import com.logitrack.dto.response.ResumenDtoResponse.*;
import com.logitrack.model.*;
import com.logitrack.repository.*;
import com.logitrack.service.ConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ConsultaServiceImpl implements ConsultaService {
    private final AuditoriaRepository auditoriaRepository;
    private final BodegaRepository bodegaRepository;
    private final ExistenciaRepository existenciaRepository;
    private final ProductoRepository productoRepository;
    private final DetalleMovimientoRepository detalleRepository;
    @Override public List<Auditoria> auditorias(String usuario, Operacion operacion) {
        return auditoriaRepository.filtrar(usuario, operacion);
    }
    @Override public ResumenDtoResponse resumen() {
        List<StockBodega> bodegas = bodegaRepository.findAll().stream().map(b -> new StockBodega(b.getId(), b.getNombre(),
            existenciaRepository.findByBodegaId(b.getId()).stream().mapToLong(e -> e.getCantidad()).sum())).toList();
        List<ProductoMovido> productos = detalleRepository.productosMasMovidos().stream().map(p -> new ProductoMovido(
            p.getProductoId(), productoRepository.findById(p.getProductoId()).orElseThrow().getNombre(), p.getCantidad())).toList();
        return new ResumenDtoResponse(bodegas, productos);
    }
}
