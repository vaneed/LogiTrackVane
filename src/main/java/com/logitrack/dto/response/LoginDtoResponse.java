package com.logitrack.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.logitrack.model.*;
public record LoginDtoResponse(String token, String tipo, long expiraEnSegundos, String nombre, Rol rol) {
    
}
