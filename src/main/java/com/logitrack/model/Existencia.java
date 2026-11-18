package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "existencia", uniqueConstraints = @UniqueConstraint(columnNames = {"bodegaId", "productoId"}))
@Getter @Setter @NoArgsConstructor
public class Existencia extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long bodegaId;
    @Column(nullable = false) private Long productoId;
    @Column(nullable = false) private Integer cantidad = 0;
}
