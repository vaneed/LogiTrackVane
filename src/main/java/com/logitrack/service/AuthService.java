package com.logitrack.service;

import com.logitrack.dto.request.*;
import com.logitrack.dto.response.LoginDtoResponse;
public interface AuthService {
    LoginDtoResponse registrar(RegistroDtoRequest dto);
    LoginDtoResponse login(LoginDtoRequest dto);
}
