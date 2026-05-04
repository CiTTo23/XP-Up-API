package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.generated.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

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
        response.setIdJuegoApi(publicacion.getIdJuegoApi());
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
        response.setIdJuegoApi(publicacion.getIdJuegoApi());
        response.setLikedByUser(likedByUser);
        response.setSavedByUser(savedByUser);
        return response;
    }

    public void updatePublicacionFromRequest(
            Publicacion publicacion,
            InternalPostUpdateRequest request
    ) {
        publicacion.setTitulo(request.getTitulo());
        publicacion.setDescripcion(request.getDescripcion());
        publicacion.setNombreJuego(request.getNombreJuego());
        publicacion.setIdJuegoApi(request.getIdJuegoApi());
        publicacion.setPortadaJuegoUrl(request.getPortadaJuegoUrl());
        publicacion.setArchivoUrl(request.getArchivoUrl());
        publicacion.setMiniaturaUrl(request.getMiniaturaUrl());
    }

    public InternalPagedPostResponse toPagedPostResponse(
            List<InternalPostSummaryResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        InternalPagedPostResponse response = new InternalPagedPostResponse();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);
        return response;
    }
}