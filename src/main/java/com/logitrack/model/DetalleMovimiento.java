package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "detalle_movimiento")
@Getter @Setter @NoArgsConstructor
public class DetalleMovimiento extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long movimientoId;
    @Column(nullable = false) private Long productoId;
    @Column(nullable = false) private Integer cantidad;
}
