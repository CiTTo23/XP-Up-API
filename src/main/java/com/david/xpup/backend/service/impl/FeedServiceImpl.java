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
        //Obtenemos el usuario autenticado, necesario para calcular el estado de interacción con cada publicación
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Si no se reciben page o size, aplicamos valores por defecto
        int numeroPagina = page != null ? page : 0;
        int tamPagina = size != null ? size : 10;

        Pageable pageable = PageRequest.of(numeroPagina, tamPagina);

        Page<Publicacion> paginaPublicaciones;

        //Si se recibe nombreJuego, filtramos el feed por ese videojuego
        if (nombreJuego != null && !nombreJuego.isBlank()) {
            paginaPublicaciones = publicacionRepository
                    .findByNombreJuegoIgnoreCaseOrderByFechaPublicacionDesc(nombreJuego, pageable);
        } else {
            //Si no se recibe filtro, devolvemos todas las publicaciones ordenadas por fecha descendente
            paginaPublicaciones = publicacionRepository.findAllByOrderByFechaPublicacionDesc(pageable);
        }

        //Transformamos cada publicación en su DTO resumen enriquecido
        List<InternalPostSummaryResponse> publicacionesResponse = paginaPublicaciones.getContent().stream()
                .map(publicacion -> mapToPostSummary(publicacion, usuarioAutenticado))
                .toList();

        //Construimos y devolvemos la respuesta paginada final
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
        //Buscamos el usuario del que se quiere obtener el feed de seguidos
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        //Obtenemos el usuario autenticado, necesario para calcular el estado de interacción con cada publicación
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Si no se reciben page o size, aplicamos valores por defecto
        int numeroPagina = page != null ? page : 0;
        int tamPagina = size != null ? size : 10;

        Pageable pageable = PageRequest.of(numeroPagina, tamPagina);

        //Obtenemos la lista de usuarios seguidos por el usuario indicado
        List<Usuario> usuariosSeguidos = seguimientoRepository.findBySeguidorOrderByFechaSeguimientoDesc(usuario)
                .stream()
                .map(Seguimiento::getSeguido)
                .toList();

        //Recuperamos las publicaciones de esos usuarios ordenadas por fecha descendente
        Page<Publicacion> paginaPublicaciones = publicacionRepository
                .findByUsuarioInOrderByFechaPublicacionDesc(usuariosSeguidos, pageable);

        //Transformamos cada publicación en su DTO resumen enriquecido
        List<InternalPostSummaryResponse> publicacionesResponse = paginaPublicaciones.getContent().stream()
                .map(publicacion -> mapToPostSummary(publicacion, usuarioAutenticado))
                .toList();

        //Construimos y devolvemos la respuesta paginada final
        return publicacionMapper.toPagedPostResponse(
                publicacionesResponse,
                paginaPublicaciones.getNumber(),
                paginaPublicaciones.getSize(),
                paginaPublicaciones.getTotalElements(),
                paginaPublicaciones.getTotalPages()
        );
    }

    //Construye el DTO resumen de una publicación añadiendo datos del autor, estadísticas y estado de interacción
    private InternalPostSummaryResponse mapToPostSummary(Publicacion publicacion, Usuario usuarioAutenticado) {
        //Obtenemos el autor de la publicación
        Usuario autor = publicacion.getUsuario();

        //Obtenemos la experiencia del autor para construir el resumen de usuario con nivel
        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

        //Calculamos estadísticas básicas de la publicación
        long totalLikes = likeRepository.countByPublicacion(publicacion);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

        //Calculamos el estado de interacción del usuario autenticado con la publicación
        boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
        boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

        //Construimos y devolvemos el DTO resumen enriquecido
        return publicacionMapper.toPostSummaryResponse(
                publicacion,
                usuarioResumen,
                totalLikes,
                totalComentarios,
                likedByUser,
                savedByUser
        );
    }

    //Obtiene el usuario autenticado actual a partir del SecurityContext de Spring Security
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