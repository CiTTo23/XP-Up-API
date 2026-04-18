/***********************************************************************************************************************
*   Mapper encargado de transformar entidades del bloque de experiencia a DTOs de la API del sistema XP-Up           *
*                                                                                                                      *
*   Forma parte de la capa de transformación entre la lógica interna del sistema y el modelo externo definido         *
*   en el API Contract (OpenAPI). Centraliza la construcción de respuestas relacionadas con la experiencia            *
*   y nivel de los usuarios                                                                                            *
*                                                                                                                      *
*   Responsabilidades principales:                                                                                     *
*       - Construir la respuesta de experiencia de un usuario                                                          *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - Aplica lógica de fallback para evitar nulls en caso de que el usuario aún no tenga experiencia registrada   *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.InternalExperienceResponse;
import org.springframework.stereotype.Component;

@Component
public class ExperienciaMapper {

    //Construye la respuesta de experiencia de un usuario -> GET /api/experience/{userId}
    public InternalExperienceResponse toExperienceResponse(Usuario usuario, Experiencia experiencia) {
        InternalExperienceResponse response = new InternalExperienceResponse();
        response.setUserId(usuario.getId());
        response.setXpTotal(experiencia != null ? experiencia.getXpTotal() : 0);
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);
        return response;
    }
}