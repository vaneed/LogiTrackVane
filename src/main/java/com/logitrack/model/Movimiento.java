package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "movimiento")
@Getter @Setter @NoArgsConstructor
public class Movimiento extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private java.time.LocalDateTime fecha;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private TipoMovimiento tipo;
    @Column(nullable = false, length = 50) private String usuario;
    private Long bodegaOrigenId;
    private Long bodegaDestinoId;
}
