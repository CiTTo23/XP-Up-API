/***********************************************************************************************************************
 *   Implementación del Publicacion Service del sistema XP-Up                                                           *
 *                                                                                                                      *
 *   Esta clase contiene la lógica asociada a la creación, consulta, edición y eliminación de publicaciones dentro de   *
 *   la plataforma                                                                                                      *
 *                                                                                                                      *
 *   Métodos principales:                                                                                               *
 *       - Crear una nueva publicación                                                                                  *
 *       - Obtener el detalle completo de una publicación                                                               *
 *       - Editar una publicación existente                                                                             *
 *       - Eliminar una publicación                                                                                     *
 *                                                                                                                      *
 *   Para ello, coordina diferentes repositorios del sistema y aplica validaciones de existencia y permisos            *
 *   antes de ejecutar operaciones sobre las publicaciones                                                              *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.PublicacionMapper;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.*;
import com.david.xpup.backend.service.PublicacionService;
import com.david.xpup.generated.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.david.xpup.backend.service.ExperienciaService;

import java.time.LocalDateTime;

@Service
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final LikeRepository likeRepository;
    private final ComentarioRepository comentarioRepository;
    private final GuardadoRepository guardadoRepository;
    private final UsuarioMapper usuarioMapper;
    private final PublicacionMapper publicacionMapper;
    private final ExperienciaService experienciaService;

    public PublicacionServiceImpl(
            PublicacionRepository publicacionRepository,
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            LikeRepository likeRepository,
            ComentarioRepository comentarioRepository,
            GuardadoRepository guardadoRepository,
            UsuarioMapper usuarioMapper,
            PublicacionMapper publicacionMapper,
            ExperienciaService experienciaService
    ) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.likeRepository = likeRepository;
        this.comentarioRepository = comentarioRepository;
        this.guardadoRepository = guardadoRepository;
        this.usuarioMapper = usuarioMapper;
        this.publicacionMapper = publicacionMapper;
        this.experienciaService = experienciaService;
    }

    //Crea una nueva publicación en el sistema -> POST /api/posts
    @Transactional
    @Override
    public InternalPostCreateResponse createPost(InternalPostRequest request) {
        //Obtenemos el usuario autenticado para impedir que se creen publicaciones a nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("You do not have permission to create posts as another user.");
        }

        //Buscamos el usuario autor de la publicación a partir del id recibido en la request
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getIdUsuario()
                ));

        //Construimos la entidad Publicacion a partir del request recibido
        Publicacion publicacion = Publicacion.builder()
                .usuario(usuario)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipoContenido(request.getTipoContenido().name())
                .idJuegoApi(request.getIdJuegoApi())
                .nombreJuego(request.getNombreJuego())
                .portadaJuegoUrl(request.getPortadaJuegoUrl())
                .archivoUrl(request.getArchivoUrl())
                .miniaturaUrl(request.getMiniaturaUrl())
                .fechaPublicacion(LocalDateTime.now())
                .build();

        //Guardamos la publicación en base de datos
        Publicacion publicacionGuardada = publicacionRepository.save(publicacion);

        //Añadimos experiencia al usuario por crear una publicación
        experienciaService.addExperienceForPost(usuario);

        //Construimos la respuesta de creación con el id generado
        InternalPostCreateResponse response = new InternalPostCreateResponse();
        response.setId(publicacionGuardada.getId());
        response.setMensaje("Post created successfully.");

        return response;
    }

    @Override
    public InternalPostDetailResponse getPostById(Integer postId) {
        //Buscamos la publicación y lanzamos excepción 404 si no existe
        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        //Obtenemos el usuario autenticado, necesario para calcular el estado de interacción con la publicación
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Obtenemos el autor de la publicación
        Usuario autor = publicacion.getUsuario();

        //Obtenemos la experiencia del autor para construir el resumen de usuario con nivel
        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

        //Calculamos estadísticas e interacción del usuario autenticado con la publicación
        long totalLikes = likeRepository.countByPublicacion(publicacion);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacion);
        boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
        boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

        //Construimos y devolvemos el detalle completo de la publicación
        return publicacionMapper.toPostDetailResponse(
                publicacion,
                usuarioResumen,
                totalLikes,
                totalComentarios,
                likedByUser,
                savedByUser
        );
    }

    //Elimina una publicación del sistema -> DELETE /api/posts/{postId}
    @Transactional
    @Override
    public MessageResponse deletePost(Integer postId) {
        //Buscamos la publicación y lanzamos excepción 404 si no existe
        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        //Obtenemos el usuario autenticado para comprobar permisos
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Solo el autor de la publicación puede eliminarla
        if (!publicacion.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this post.");
        }

        //Eliminamos primero los registros relacionados para evitar errores de clave foránea
        likeRepository.deleteByPublicacion(publicacion);
        comentarioRepository.deleteByPublicacion(publicacion);
        guardadoRepository.deleteByPublicacion(publicacion);

        //Eliminamos la publicación de BD
        publicacionRepository.delete(publicacion);

        //Construimos la respuesta de éxito
        MessageResponse response = new MessageResponse();
        response.setMensaje("Post deleted successfully.");

        return response;
    }

    //Actualiza los datos editables de una publicación -> PUT /api/posts/{postId}
    @Transactional
    @Override
    public InternalPostDetailResponse updatePost(Integer postId, InternalPostUpdateRequest request) {
        //Buscamos la publicación y lanzamos excepción 404 si no existe
        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        //Obtenemos el usuario autenticado para comprobar permisos
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Solo el autor de la publicación puede editarla
        if (!publicacion.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new UnauthorizedException("You do not have permission to edit this post.");
        }

        //Actualizamos los datos editables de la publicación
        updatePostFields(publicacion, request);

        //Guardamos la publicación actualizada en base de datos
        Publicacion publicacionActualizada = publicacionRepository.save(publicacion);

        //Obtenemos el autor de la publicación
        Usuario autor = publicacionActualizada.getUsuario();

        //Obtenemos la experiencia del autor para construir el resumen de usuario con nivel
        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

        //Calculamos estadísticas e interacción del usuario autenticado con la publicación
        long totalLikes = likeRepository.countByPublicacion(publicacionActualizada);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacionActualizada);
        boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacionActualizada);
        boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacionActualizada);

        //Construimos y devolvemos el detalle completo de la publicación actualizada
        return publicacionMapper.toPostDetailResponse(
                publicacionActualizada,
                usuarioResumen,
                totalLikes,
                totalComentarios,
                likedByUser,
                savedByUser
        );
    }

    //Actualiza los campos editables de una publicación a partir de los datos recibidos en la request.
    private void updatePostFields(
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