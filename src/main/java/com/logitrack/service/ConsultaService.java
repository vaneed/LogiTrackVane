package com.logitrack.service;

import com.logitrack.dto.response.ResumenDtoResponse;
import com.logitrack.model.*;
import java.util.List;
public interface ConsultaService {
    List<Auditoria> auditorias(String usuario, Operacion operacion);
    ResumenDtoResponse resumen();
}
