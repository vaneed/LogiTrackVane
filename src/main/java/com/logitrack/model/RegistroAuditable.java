package com.logitrack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logitrack.config.AuditoriaListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@MappedSuperclass
@EntityListeners(AuditoriaListener.class)
@Getter @Setter
public abstract class RegistroAuditable {
    @Transient @JsonIgnore
    private String valoresAnteriores;
}
