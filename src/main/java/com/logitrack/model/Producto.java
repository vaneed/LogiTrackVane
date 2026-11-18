package com.logitrack.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "producto")
@Getter @Setter @NoArgsConstructor
public class Producto extends RegistroAuditable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80) private String nombre;
    @Column(nullable = false, length = 60) private String categoria;
    @Column(nullable = false) private Integer stock = 0;
    @Column(nullable = false, precision = 12, scale = 2) private java.math.BigDecimal precio;
}
