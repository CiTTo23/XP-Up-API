/***********************************************************************************************************************
 *   Implementación del Feed Service del sistema XP-Up                                                                  *
 *                                                                                                                      *
 *   Esta clase contiene la lógica asociada a la obtención del feed general de publicaciones y del feed de usuarios    *
 *   seguidos dentro de la plataforma                                                                                   *
 *                                                                                                                      *
 *   Métodos principales:                                                                                               *
 *       - Obtener el feed general paginado de publicaciones                                                            *
 *       - Obtener el feed paginado de publicaciones de usuarios seguidos                                               *
 *                                                                                                                      *
 *   Para ello, coordina diferentes repositorios del sistema y construye respuestas enriquecidas con información       *
 *   del autor, estadísticas de interacción y estado de interacción del usuario autenticado                            *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Seguimiento;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.PublicacionMapper;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.ComentarioRepository;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.GuardadoRepository;
import com.david.xpup.backend.repository.LikeRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.SeguimientoRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.FeedService;
import com.david.xpup.generated.model.InternalPagedPostResponse;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedServiceImpl implements FeedService {

    private final PublicacionRepository publicacionRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final LikeRepository likeRepository;
    private final ComentarioRepository comentarioRepository;
    private final GuardadoRepository guardadoRepository;
    private final UsuarioMapper usuarioMapper;
    private final PublicacionMapper publicacionMapper;

    public FeedServiceImpl(
            PublicacionRepository publicacionRepository,
            SeguimientoRepository seguimientoRepository,
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            LikeRepository likeRepository,
            ComentarioRepository comentarioRepository,
            GuardadoRepository guardadoRepository,
            UsuarioMapper usuarioMapper,
            PublicacionMapper publicacionMapper
    ) {
        this.publicacionRepository = publicacionRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.likeRepository = likeRepository;
        this.comentarioRepository = comentarioRepository;
        this.guardadoRepository = guardadoRepository;
        this.usuarioMapper = usuarioMapper;
        this.publicacionMapper = publicacionMapper;
    }

    //Obtiene el feed general paginado de publicaciones ordenado por fecha -> GET /api/feed
    @Override
    public InternalPagedPostResponse getFeed(String orden, Integer page, Integer size, String nombreJuego) {
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        int numeroPagina = page != null ? page : 0;
        int tamPagina = size != null ? size : 10;

        Pageable pageable = PageRequest.of(numeroPagina, tamPagina);

        Page<Publicacion> paginaPublicaciones;

        if (nombreJuego != null && !nombreJuego.isBlank()) {
            paginaPublicaciones = publicacionRepository
                    .findByNombreJuegoContainingIgnoreCaseOrderByFechaPublicacionDesc(
                            nombreJuego.trim(),
                            pageable
                    );
        } else {
            paginaPublicaciones = publicacionRepository.findAllByOrderByFechaPublicacionDesc(pageable);
        }

        List<InternalPostSummaryResponse> publicacionesResponse = paginaPublicaciones.getContent().stream()
                .map(publicacion -> mapToPostSummary(publicacion, usuarioAutenticado))
                .toList();

        return publicacionMapper.toPagedPostResponse(
                publicacionesResponse,
                paginaPublicaciones.getNumber(),
                paginaPublicaciones.getSize(),
                paginaPublicaciones.getTotalElements(),
                paginaPublicaciones.getTotalPages()
        );
    }

    //Obtiene el feed paginado de publicaciones de usuarios seguidos ordenado por fecha -> GET /api/feed/following/{userId}
    @Override
    public InternalPagedPostResponse getFollowingFeed(Integer userId, String orden, Integer page, Integer size) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        int numeroPagina = page != null ? page : 0;
        int tamPagina = size != null ? size : 10;

        Pageable pageable = PageRequest.of(numeroPagina, tamPagina);

        List<Usuario> usuariosSeguidos = seguimientoRepository.findBySeguidorOrderByFechaSeguimientoDesc(usuario)
                .stream()
                .map(Seguimiento::getSeguido)
                .toList();

        Page<Publicacion> paginaPublicaciones = publicacionRepository
                .findByUsuarioInOrderByFechaPublicacionDesc(usuariosSeguidos, pageable);

        List<InternalPostSummaryResponse> publicacionesResponse = paginaPublicaciones.getContent().stream()
                .map(publicacion -> mapToPostSummary(publicacion, usuarioAutenticado))
                .toList();

        return publicacionMapper.toPagedPostResponse(
                publicacionesResponse,
                paginaPublicaciones.getNumber(),
                paginaPublicaciones.getSize(),
                paginaPublicaciones.getTotalElements(),
                paginaPublicaciones.getTotalPages()
        );
    }

    private InternalPostSummaryResponse mapToPostSummary(Publicacion publicacion, Usuario usuarioAutenticado) {
        Usuario autor = publicacion.getUsuario();

        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

        long totalLikes = likeRepository.countByPublicacion(publicacion);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

        boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
        boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

        return publicacionMapper.toPostSummaryResponse(
                publicacion,
                usuarioResumen,
                totalLikes,
                totalComentarios,
                likedByUser,
                savedByUser
        );
    }

    private Usuario getAuthenticatedUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        String nombreUsuario = authentication.getName();

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found."));
    }
}