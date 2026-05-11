/***********************************************************************************************************************
 *   Mapper encargado de transformar la entidad SkinPerfil en DTOs relacionados con skins de perfil                    *
 *                                                                                                                      *
 *   Forma parte de la capa de transformación entre el modelo interno de entidades JPA y el modelo externo definido     *
 *   en el API Contract                                                                                                 *
 *                                                                                                                      *
 *   Responsabilidades principales:                                                                                     *
 *       - Convertir una SkinPerfil en la respuesta básica de skin activa del perfil                                    *
 *       - Convertir una SkinPerfil en una respuesta enriquecida con estado de desbloqueo y equipamiento                *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.SkinPerfil;
import com.david.xpup.generated.model.InternalProfileSkinResponse;
import com.david.xpup.generated.model.InternalUserProfileSkinResponse;
import org.springframework.stereotype.Component;

//Bean de Spring
@Component
public class SkinPerfilMapper {

    //Convierte entity SkinPerfil en InternalProfileSkinResponse -> skin activa incluida dentro del perfil de usuario
    public InternalProfileSkinResponse toProfileSkinResponse(SkinPerfil skinPerfil) {
        if (skinPerfil == null) {
            return null;
        }

        InternalProfileSkinResponse response = new InternalProfileSkinResponse();
        response.setId(skinPerfil.getId());
        response.setCodigo(skinPerfil.getCodigo());
        response.setNombre(skinPerfil.getNombre());
        response.setDescripcion(skinPerfil.getDescripcion());
        response.setNivelRequerido(skinPerfil.getNivelRequerido());
        response.setFondoUrlOscuro(skinPerfil.getFondoUrlOscuro());
        response.setFondoUrlClaro(skinPerfil.getFondoUrlClaro());
        response.setActiva(skinPerfil.getActiva());
        return response;
    }

    //Convierte entity SkinPerfil en InternalUserProfileSkinResponse -> skin con estado calculado para un usuario concreto
    public InternalUserProfileSkinResponse toUserProfileSkinResponse(
            SkinPerfil skinPerfil,
            int nivelUsuario,
            Integer idSkinEquipada
    ) {
        boolean desbloqueada = nivelUsuario >= skinPerfil.getNivelRequerido();
        boolean equipada = idSkinEquipada != null && idSkinEquipada.equals(skinPerfil.getId());

        InternalUserProfileSkinResponse response = new InternalUserProfileSkinResponse();
        response.setId(skinPerfil.getId());
        response.setCodigo(skinPerfil.getCodigo());
        response.setNombre(skinPerfil.getNombre());
        response.setDescripcion(skinPerfil.getDescripcion());
        response.setNivelRequerido(skinPerfil.getNivelRequerido());
        response.setFondoUrlOscuro(skinPerfil.getFondoUrlOscuro());
        response.setFondoUrlClaro(skinPerfil.getFondoUrlClaro());
        response.setActiva(skinPerfil.getActiva());
        response.setDesbloqueada(desbloqueada);
        response.setEquipada(equipada);
        return response;
    }
}