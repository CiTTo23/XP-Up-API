package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.generated.model.InternalPostDetailResponse;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class PublicacionMapper {

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

    public InternalPostDetailResponse toPostDetailResponse(
            Publicacion publicacion,
            InternalUserSummaryResponse usuarioResumen,
            long totalLikes,
            long totalComentarios,
            boolean likedByUser,
            boolean savedByUser
    ) {
        InternalPostDetailResponse response = new InternalPostDetailResponse();
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