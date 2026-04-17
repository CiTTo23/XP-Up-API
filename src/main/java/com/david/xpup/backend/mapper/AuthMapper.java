/***********************************************************************************************************************
*   Mapper encargado de transformar la entidad Usuario en DTOs relacionados con autenticación                          *
*                                                                                                                      *
*   Forma parte de la capa de transformación entre el modelo interno (entidades JPA) y el modelo externo definido      *
*   en el API Contract (OpenAPI), específicamente en los procesos de registro y autenticación                          *
*                                                                                                                      *
*   Responsabilidades principales:                                                                                     *
*       - Convertir un Usuario en la respuesta tras un registro (AuthRegisterResponse)                                 *
*       - Convertir un Usuario en un objeto Usuario autenticado (AuthUserResponse)                                     *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - No expone información sensible como la contraseña                                                            *
*       - Se utiliza en los endpoints /api/auth/register y /api/auth/login                                             *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.AuthRegisterResponse;
import com.david.xpup.generated.model.AuthUserResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

//Bean de Spring
@Component
public class AuthMapper {

    //Convierte la entity Usuario en AuthRegisterResponse -> para poder realizar el registro correctamente
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

    //Convierte la entity Usuario en AuthUserResponse -> Usuario con rol, ya autenticado
    public AuthUserResponse toAuthUserResponse(Usuario usuario) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        return response;
    }
}