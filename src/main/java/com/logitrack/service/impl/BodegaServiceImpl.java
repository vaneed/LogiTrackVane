package com.logitrack.service.impl;

import com.logitrack.dto.request.BodegaDtoRequest;
import com.logitrack.dto.response.*;
import com.logitrack.exception.*;
import com.logitrack.mapper.BodegaMapper;
import com.logitrack.model.Bodega;
import com.logitrack.repository.*;
import com.logitrack.service.BodegaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class BodegaServiceImpl implements BodegaService {
    private final BodegaRepository bodegaRepository;
    private final ExistenciaRepository existenciaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final BodegaMapper bodegaMapper;
    @Override @Transactional
    public BodegaDtoResponse guardar(BodegaDtoRequest dto) {
        Bodega bodega = bodegaMapper.dtoToEntity(dto);
        return bodegaMapper.entityToDto(bodegaRepository.save(bodega));
    }
    @Override @Transactional
    public BodegaDtoResponse actualizar(BodegaDtoRequest dto, Long id) {
        bodegaRepository.bloquearTodas();
        Bodega bodega = buscar(id);
        long ocupado = existenciaRepository.findByBodegaId(id).stream().mapToLong(e -> e.getCantidad()).sum();
        if (dto.capacidad() < ocupado) throw new ReglaNegocioException("La capacidad no puede ser menor al stock almacenado.");
        bodegaMapper.updateEntityFromDto(bodega, dto);
        return bodegaMapper.entityToDto(bodegaRepository.save(bodega));
    }
    @Override @Transactional
    public void eliminar(Long id) {
        bodegaRepository.bloquearTodas();
        Bodega bodega = buscar(id);
        if (existenciaRepository.existsByBodegaId(id) || movimientoRepository.existsByBodegaOrigenIdOrBodegaDestinoId(id, id))
            throw new ReglaNegocioException("La bodega tiene historial de inventario y no se puede eliminar.");
        bodegaRepository.delete(bodega);
    }
    @Override public List<BodegaDtoResponse> listar() { return bodegaRepository.findAll().stream().map(bodegaMapper::entityToDto).toList(); }
    @Override public BodegaDtoResponse buscarId(Long id) { return bodegaMapper.entityToDto(buscar(id)); }
    @Override public List<ExistenciaDtoResponse> existencias(Long id) {
        buscar(id);
        return existenciaRepository.findByBodegaId(id).stream().map(e -> new ExistenciaDtoResponse(
            e.getProductoId(), productoRepository.findById(e.getProductoId()).orElseThrow().getNombre(), e.getCantidad())).toList();
    }
    private Bodega buscar(Long id) { return bodegaRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Bodega no encontrada.")); }
}
