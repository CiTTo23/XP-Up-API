/***********************************************************************************************************************
*   Mapper encargado de transformar resultados del bloque de seguimientos a DTOs de la API del sistema XP-Up         *
*                                                                                                                      *
*   Forma parte de la capa de transformación entre la lógica interna del sistema y el modelo externo definido         *
*   en el API Contract (OpenAPI). Centraliza la construcción de las respuestas relacionadas con seguimientos          *
*                                                                                                                      *
*   Responsabilidades principales:                                                                                     *
*       - Construir respuestas simples de mensaje para seguir o dejar de seguir                                        *
*       - Construir la respuesta de comprobación de seguimiento entre usuarios                                          *
*       - Construir la respuesta de estadísticas de seguidores y seguidos de un usuario                                *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - Evita duplicar la creación manual de DTOs en la capa service                                                 *
*       - Mantiene consistente el formato de respuesta del bloque de seguimientos                                      *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.generated.model.InternalFollowCheckResponse;
import com.david.xpup.generated.model.InternalFollowStatsResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class SeguimientoMapper {

    //Construye una respuesta simple con mensaje de éxito -> follow / unfollow
    public MessageResponse toMessageResponse(String mensaje) {
        MessageResponse response = new MessageResponse();
        response.setMensaje(mensaje);
        return response;
    }

    //Construye la respuesta que indica si un usuario sigue a otro -> GET /api/follows/check
    public InternalFollowCheckResponse toFollowCheckResponse(boolean following) {
        InternalFollowCheckResponse response = new InternalFollowCheckResponse();
        response.setFollowing(following);
        return response;
    }

    //Construye la respuesta con estadísticas de seguidores y seguidos -> GET /api/follows/stats/{userId}
    public InternalFollowStatsResponse toFollowStatsResponse(long totalSeguidores, long totalSeguidos) {
        InternalFollowStatsResponse response = new InternalFollowStatsResponse();
        response.setTotalSeguidores((int) totalSeguidores);
        response.setTotalSeguidos((int) totalSeguidos);
        return response;
    }
}