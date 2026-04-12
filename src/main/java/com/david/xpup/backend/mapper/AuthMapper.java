package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.AuthRegisterResponse;
import com.david.xpup.generated.model.AuthUserResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class AuthMapper {

    public AuthRegisterResponse toAuthRegisterResponse(Usuario usuario) {
        AuthRegisterResponse response = new AuthRegisterResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setFechaRegistro(usuario.getFechaRegistro().atOffset(ZoneOffset.UTC));
        response.setMensaje("Usuario registrado correctamente");
        return response;
    }

    public AuthUserResponse toAuthUserResponse(Usuario usuario) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        return response;
    }
}