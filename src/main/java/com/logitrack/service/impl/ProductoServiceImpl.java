package com.logitrack.service.impl;

import com.logitrack.dto.request.ProductoDtoRequest;
import com.logitrack.dto.response.ProductoDtoResponse;
import com.logitrack.exception.*;
import com.logitrack.mapper.ProductoMapper;
import com.logitrack.model.Producto;
import com.logitrack.repository.*;
import com.logitrack.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final ExistenciaRepository existenciaRepository;
    private final DetalleMovimientoRepository detalleRepository;
    private final ProductoMapper productoMapper;
    @Override @Transactional
    public ProductoDtoResponse guardar(ProductoDtoRequest dto) {
        Producto producto = productoMapper.dtoToEntity(dto);
        return productoMapper.entityToDto(productoRepository.save(producto));
    }
    @Override @Transactional
    public ProductoDtoResponse actualizar(ProductoDtoRequest dto, Long id) {
        bodegaRepository.bloquearTodas();
        Producto producto = buscar(id);
        productoMapper.updateEntityFromDto(producto, dto);
        return productoMapper.entityToDto(productoRepository.save(producto));
    }
    @Override @Transactional
    public void eliminar(Long id) {
        bodegaRepository.bloquearTodas();
        Producto producto = buscar(id);
        if (existenciaRepository.existsByProductoId(id) || detalleRepository.existsByProductoId(id))
            throw new ReglaNegocioException("El producto tiene historial de inventario y no se puede eliminar.");
        productoRepository.delete(producto);
    }
    @Override public List<ProductoDtoResponse> listar() { return productoRepository.findAll().stream().map(productoMapper::entityToDto).toList(); }
    @Override public ProductoDtoResponse buscarId(Long id) { return productoMapper.entityToDto(buscar(id)); }
    @Override public List<ProductoDtoResponse> stockBajo() { return productoRepository.findByStockLessThan(10).stream().map(productoMapper::entityToDto).toList(); }
    private Producto buscar(Long id) { return productoRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Producto no encontrado.")); }
}
