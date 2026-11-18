package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "bodega")
@Getter @Setter @NoArgsConstructor
public class Bodega extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80) private String nombre;
    @Column(nullable = false, length = 150) private String ubicacion;
    @Column(nullable = false) private Integer capacidad;
    @Column(nullable = false, length = 80) private String encargado;
}
