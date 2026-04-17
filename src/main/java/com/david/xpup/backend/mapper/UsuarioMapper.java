/***********************************************************************************************************************
*   Mapper encargado de transformar entidades del dominio relacionadas con usuarios y publicaciones a DTOs de la API  *
*                                                                                                                     *
*   Forma parte de la capa de transformación entre el modelo interno (entidades JPA) y el modelo externo definido     *
*   en el API Contract (OpenAPI). Centraliza toda la lógica de conversión                                             *
*                                                                                                                     *
*   Responsabilidades principales:                                                                                    *
*       - Convertir un Usuario + Experiencia en un resumen de usuario (para listados)                                 *
*       - Convertir un Usuario + datos agregados en un perfil completo                                                *
*       - Convertir una Publicacion en un resumen enriquecido (feed, guardados, likes, etc.)                          *
*                                                                                                                     *
*   Importante:                                                                                                       *
*       - Recibe datos agregados (likes, seguidores, etc.) desde la capa service                                      *
*       - Aplica lógica de fallback para evitar nulls (ej: nivel por defecto = 1)                                     *
*                                                                                                                     *
***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

//Bean de Spring
@Component
public class UsuarioMapper {

    //Convierte entity Usuario + Experiencia en InternalUserSummaryResponse -> feed, listas de seguidores/seguidos, comentarios
    public InternalUserSummaryResponse toUserSummaryResponse(Usuario usuario, Experiencia experiencia) {
        InternalUserSummaryResponse response = new InternalUserSummaryResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);
        return response;
    }

    //Construye el DTO más completo de un usuario. Mapea entity Usuario + Seguidores + Seguidos + Publicaciones
    public InternalUserProfileResponse toUserProfileResponse(
            Usuario usuario,
            Experiencia experiencia,
            long totalSeguidores,
            long totalSeguidos,
            long totalPublicaciones
    ) {
        InternalUserProfileResponse response = new InternalUserProfileResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setEmail(usuario.getEmail());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setBiografia(usuario.getBiografia());
        response.setRol(usuario.getRol());
        response.setFechaRegistro(usuario.getFechaRegistro().atOffset(ZoneOffset.UTC));//conversion LocalDateTime -> OffsetDateTime
        response.setXpTotal(experiencia != null ? experiencia.getXpTotal() : 0);
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);
        response.setTotalSeguidores((int) totalSeguidores);
        response.setTotalSeguidos((int) totalSeguidos);
        response.setTotalPublicaciones((int) totalPublicaciones);
        return response;
    }


    // MOVER A PUBLICACION MAPPER
    public InternalPostSummaryResponse toPostSummaryResponse(
            Publicacion publicacion,
            InternalUserSummaryResponse usuarioResumen,
            long totalLikes,
            long totalComentarios,
            boolean likedByUser,
            boolean savedByUser
    ) {
        InternalPostSummaryResponse response = new InternalPostSummaryResponse();
        response.setId(publicacion.getId());
        response.setUsuario(usuarioResumen);
        response.setTitulo(publicacion.getTitulo());
        response.setDescripcion(publicacion.getDescripcion());
        response.setTipoContenido(publicacion.getTipoContenido());
        response.setArchivoUrl(publicacion.getArchivoUrl());
        response.setMiniaturaUrl(publicacion.getMiniaturaUrl());
        response.setNombreJuego(publicacion.getNombreJuego());
        response.setPortadaJuegoUrl(publicacion.getPortadaJuegoUrl());
        response.setFechaPublicacion(publicacion.getFechaPublicacion().atOffset(ZoneOffset.UTC));
        response.setTotalLikes((int) totalLikes);
        response.setTotalComentarios((int) totalComentarios);
        response.setLikedByUser(likedByUser);
        response.setSavedByUser(savedByUser);
        return response;
    }
}