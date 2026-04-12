package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class UsuarioMapper {

    public InternalUserSummaryResponse toUserSummaryResponse(Usuario usuario, Experiencia experiencia) {
        InternalUserSummaryResponse response = new InternalUserSummaryResponse();
        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);
        return response;
    }

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
        response.setFechaRegistro(usuario.getFechaRegistro().atOffset(ZoneOffset.UTC));
        response.setXpTotal(experiencia != null ? experiencia.getXpTotal() : 0);
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);
        response.setTotalSeguidores((int) totalSeguidores);
        response.setTotalSeguidos((int) totalSeguidos);
        response.setTotalPublicaciones((int) totalPublicaciones);
        return response;
    }

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