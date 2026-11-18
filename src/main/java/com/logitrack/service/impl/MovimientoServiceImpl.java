package com.logitrack.service.impl;

import com.logitrack.dto.request.*;
import com.logitrack.dto.response.*;
import com.logitrack.exception.*;
import com.logitrack.model.*;
import com.logitrack.repository.*;
import com.logitrack.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoServiceImpl implements MovimientoService {
    private final MovimientoRepository movimientoRepository;
    private final DetalleMovimientoRepository detalleRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final ExistenciaRepository existenciaRepository;

    @Override @Transactional
    public MovimientoDtoResponse guardar(MovimientoDtoRequest dto, String usuario) {
        validarBodegas(dto);
        // Bloqueamos en el mismo orden para que dos movimientos no gasten el mismo stock.
        // Es una solución sencilla para un proyecto pequeño: procesa un movimiento a la vez.
        bodegaRepository.bloquearTodas();
        if (dto.bodegaOrigenId() != null) buscarBodega(dto.bodegaOrigenId());
        if (dto.bodegaDestinoId() != null) buscarBodega(dto.bodegaDestinoId());
        Set<Long> ids = new HashSet<>();
        long unidades = 0;
        for (DetalleMovimientoDtoRequest detalle : dto.productos()) {
            if (!ids.add(detalle.productoId())) throw new ReglaNegocioException("No repitas un producto en el movimiento.");
            unidades += detalle.cantidad();
        }
        if (dto.bodegaDestinoId() != null) {
            Bodega destino = buscarBodega(dto.bodegaDestinoId());
            long ocupado = existenciaRepository.findByBodegaId(destino.getId()).stream().mapToLong(e -> e.getCantidad()).sum();
            if (ocupado + unidades > destino.getCapacidad()) throw new ReglaNegocioException("La bodega destino no tiene capacidad suficiente.");
        }
        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now(ZoneOffset.UTC));
        movimiento.setTipo(dto.tipo());
        movimiento.setUsuario(usuario);
        movimiento.setBodegaOrigenId(dto.bodegaOrigenId());
        movimiento.setBodegaDestinoId(dto.bodegaDestinoId());
        movimientoRepository.save(movimiento);
        for (DetalleMovimientoDtoRequest detalle : dto.productos()) {
            Producto producto = productoRepository.findById(detalle.productoId())
                .orElseThrow(() -> new NoEncontradoException("Producto no encontrado: " + detalle.productoId()));
            int cantidad = detalle.cantidad();
            if (dto.bodegaOrigenId() != null) restar(dto.bodegaOrigenId(), producto.getId(), cantidad);
            if (dto.bodegaDestinoId() != null) sumar(dto.bodegaDestinoId(), producto.getId(), cantidad);
            long stock = producto.getStock();
            if (dto.tipo() == TipoMovimiento.ENTRADA) stock += cantidad;
            if (dto.tipo() == TipoMovimiento.SALIDA) stock -= cantidad;
            if (stock < 0 || stock > Integer.MAX_VALUE) throw new ReglaNegocioException("El stock total está fuera del rango permitido.");
            producto.setStock((int) stock);
            productoRepository.save(producto);
            DetalleMovimiento dato = new DetalleMovimiento();
            dato.setMovimientoId(movimiento.getId());
            dato.setProductoId(producto.getId());
            dato.setCantidad(cantidad);
            detalleRepository.save(dato);
        }
        // Si cualquier producto falla, @Transactional deshace TODO, incluida la auditoría.
        return respuesta(movimiento);
    }
    private void validarBodegas(MovimientoDtoRequest dto) {
        Long origen = dto.bodegaOrigenId();
        Long destino = dto.bodegaDestinoId();
        if (dto.tipo() == TipoMovimiento.ENTRADA && (origen != null || destino == null))
            throw new ReglaNegocioException("Una entrada requiere solo bodega destino.");
        if (dto.tipo() == TipoMovimiento.SALIDA && (origen == null || destino != null))
            throw new ReglaNegocioException("Una salida requiere solo bodega origen.");
        if (dto.tipo() == TipoMovimiento.TRANSFERENCIA && (origen == null || destino == null || origen.equals(destino)))
            throw new ReglaNegocioException("Una transferencia requiere dos bodegas diferentes.");
    }
    private Bodega buscarBodega(Long id) { return bodegaRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Bodega no encontrada: " + id)); }
    private void restar(Long bodegaId, Long productoId, int cantidad) {
        Existencia dato = existenciaRepository.findByBodegaIdAndProductoId(bodegaId, productoId)
            .orElseThrow(() -> new ReglaNegocioException("El producto no tiene existencias en la bodega origen."));
        if (dato.getCantidad() < cantidad) throw new ReglaNegocioException("Stock insuficiente en la bodega origen.");
        dato.setCantidad(dato.getCantidad() - cantidad);
        existenciaRepository.save(dato);
    }
    private void sumar(Long bodegaId, Long productoId, int cantidad) {
        Existencia dato = existenciaRepository.findByBodegaIdAndProductoId(bodegaId, productoId).orElseGet(() -> {
            Existencia nueva = new Existencia();
            nueva.setBodegaId(bodegaId);
            nueva.setProductoId(productoId);
            return nueva;
        });
        dato.setCantidad(dato.getCantidad() + cantidad);
        existenciaRepository.save(dato);
    }
    @Override public List<MovimientoDtoResponse> listar(LocalDateTime desde, LocalDateTime hasta) {
        if ((desde == null) != (hasta == null)) throw new ReglaNegocioException("Envía desde y hasta juntos.");
        if (desde != null && desde.isAfter(hasta)) throw new ReglaNegocioException("Desde no puede ser posterior a hasta.");
        List<Movimiento> lista = desde == null ? movimientoRepository.findAllByOrderByFechaDesc()
            : movimientoRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta);
        return lista.stream().map(this::respuesta).toList();
    }
    @Override public MovimientoDtoResponse buscarId(Long id) {
        return respuesta(movimientoRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Movimiento no encontrado.")));
    }
    private MovimientoDtoResponse respuesta(Movimiento dato) {
        List<DetalleMovimientoDtoResponse> productos = detalleRepository.findByMovimientoId(dato.getId()).stream()
            .map(d -> new DetalleMovimientoDtoResponse(d.getProductoId(), d.getCantidad())).toList();
        return new MovimientoDtoResponse(dato.getId(), dato.getFecha(), dato.getTipo(), dato.getUsuario(), dato.getBodegaOrigenId(), dato.getBodegaDestinoId(), productos);
    }
}
