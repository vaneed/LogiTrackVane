package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "usuario")
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String nombre;
    @Column(nullable = false, length = 100) private String clave;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Rol rol;
}
