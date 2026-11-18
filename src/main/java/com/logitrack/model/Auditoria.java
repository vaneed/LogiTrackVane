package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name = "auditoria")
@Getter @Setter @NoArgsConstructor
public class Auditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private Operacion operacion;
    @Column(nullable = false) private LocalDateTime fecha;
    @Column(nullable = false, length = 50) private String usuario;
    @Column(nullable = false, length = 80) private String entidad;
    @Column(columnDefinition = "LONGTEXT") private String valoresAnteriores;
    @Column(columnDefinition = "LONGTEXT") private String valoresNuevos;
}
