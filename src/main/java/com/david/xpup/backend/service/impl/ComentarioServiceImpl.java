/***********************************************************************************************************************
 *   Implementación del Comentario Service del sistema XP-Up                                                            *
 *                                                                                                                      *
 *   Esta clase contiene la lógica asociada a la creación y consulta de comentarios dentro de la plataforma            *
 *                                                                                                                      *
 *   Métodos principales:                                                                                               *
 *       - Crear un nuevo comentario en una publicación                                                                 *
 *       - Obtener la lista de comentarios de una publicación                                                           *
 *                                                                                                                      *
 *   Para ello, coordina diferentes repositorios del sistema y aplica validaciones de existencia y permisos            *
 *   antes de ejecutar operaciones sobre los comentarios                                                                *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Comentario;
import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.ComentarioMapper;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.ComentarioRepository;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.ComentarioService;
import com.david.xpup.generated.model.InternalCommentRequest;
import com.david.xpup.generated.model.InternalCommentResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ExperienciaRepository experienciaRepository;
    private final UsuarioMapper usuarioMapper;
    private final ComentarioMapper comentarioMapper;

    public ComentarioServiceImpl(
            ComentarioRepository comentarioRepository,
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ExperienciaRepository experienciaRepository,
            UsuarioMapper usuarioMapper,
            ComentarioMapper comentarioMapper
    ) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.experienciaRepository = experienciaRepository;
        this.usuarioMapper = usuarioMapper;
        this.comentarioMapper = comentarioMapper;
    }

    //Crea un comentario en una publicación -> POST /api/comments
    @Override
    public InternalCommentResponse createComment(InternalCommentRequest request) {
        //Obtenemos el usuario autenticado para impedir que se creen comentarios a nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario enviado en la request
        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("No tienes permisos para comentar en nombre de otro usuario");
        }

        //Buscamos el usuario autor del comentario a partir del id recibido en la request
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + request.getIdUsuario()
                ));

        //Buscamos la publicación sobre la que se quiere comentar
        Publicacion publicacion = publicacionRepository.findById(request.getIdPublicacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Publicación no encontrada con id: " + request.getIdPublicacion()
                ));

        //Construimos la entidad Comentario a partir del request recibido
        Comentario comentario = Comentario.builder()
                .usuario(usuario)
                .publicacion(publicacion)
                .contenido(request.getContenido())
                .fechaComentario(LocalDateTime.now())
                .build();

        //Guardamos el comentario en base de datos
        Comentario comentarioGuardado = comentarioRepository.save(comentario);

        //Obtenemos la experiencia del autor para construir el resumen de usuario con nivel
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(usuario, experiencia);

        //Construimos y devolvemos la respuesta del comentario creado
        return comentarioMapper.toCommentResponse(comentarioGuardado, usuarioResumen);
    }

    //Devuelve los comentarios de una publicación ordenados por fecha ascendente -> GET /api/comments/post/{postId}
    @Override
    public List<InternalCommentResponse> getCommentsByPost(Integer postId) {
        //Buscamos la publicación y lanzamos excepción 404 si no existe
        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con id: " + postId));

        //Obtenemos todos los comentarios asociados a la publicación ordenados por fecha ascendente
        List<Comentario> comentarios = comentarioRepository.findByPublicacionOrderByFechaComentarioAsc(publicacion);

        //Transformamos cada comentario en su DTO respuesta junto con el resumen del usuario autor
        return comentarios.stream()
                .map(comentario -> {
                    //Obtenemos el autor del comentario
                    Usuario autor = comentario.getUsuario();

                    //Obtenemos la experiencia del autor para construir el resumen de usuario con nivel
                    Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
                    InternalUserSummaryResponse usuarioResumen =
                            usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

                    //Construimos y devolvemos el DTO de comentario
                    return comentarioMapper.toCommentResponse(comentario, usuarioResumen);
                })
                .toList();
    }

    //Obtiene el usuario autenticado actual a partir del SecurityContext de Spring Security
    private Usuario getAuthenticatedUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("Usuario no autenticado");
        }

        String nombreUsuario = authentication.getName();

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UnauthorizedException("Usuario autenticado no encontrado"));
    }
}