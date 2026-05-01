/***********************************************************************************************************************
*   Implementación del Like Service del sistema XP-Up                                                                  *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la creación de likes sobre publicaciones dentro de la plataforma         *
*                                                                                                                      *
*   Métodos principales:                                                                                               *
*       - Dar like a una publicación                                                                                   *
*                                                                                                                      *
*   Para ello, coordina diferentes repositorios del sistema y aplica validaciones de existencia, permisos y           *
*   duplicidad antes de ejecutar operaciones sobre los likes                                                           *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Like;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.repository.LikeRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.LikeService;
import com.david.xpup.generated.model.InternalLikeCheckResponse;
import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalLikesCountResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.david.xpup.backend.service.ExperienciaService;

import java.time.LocalDateTime;

@Service
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ExperienciaService experienciaService;

    public LikeServiceImpl(
            LikeRepository likeRepository,
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ExperienciaService experienciaService
    ) {
        this.likeRepository = likeRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.experienciaService = experienciaService;
    }

    //Añade un like a una publicación -> POST /api/likes
    @Transactional
    @Override
    public MessageResponse likePost(InternalLikeRequest request) {
        //Obtenemos el usuario autenticado para impedir que se den likes en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario enviado en la request
        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("You do not have permission to like posts as another user.");
        }

        //Buscamos el usuario que da like a partir del id recibido en la request
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getIdUsuario()
                ));

        //Buscamos la publicación a la que se quiere dar like
        Publicacion publicacion = publicacionRepository.findById(request.getIdPublicacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + request.getIdPublicacion()
                ));

        //Comprobamos que el like no exista ya previamente
        if (likeRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            throw new DuplicateResourceException("You have already liked this post.");
        }

        //Construimos la entidad Like a partir del usuario y la publicación
        Like like = Like.builder()
                .usuario(usuario)
                .publicacion(publicacion)
                .fechaLike(LocalDateTime.now())
                .build();

        //Guardamos el like en base de datos
        likeRepository.save(like);

        //Añadimos experiencia al usuario por dar like
        experienciaService.addExperienceForLike(usuario);

        //Construimos la respuesta de éxito
        MessageResponse response = new MessageResponse();
        response.setMensaje("Like added successfully.");

        return response;
    }


    //Elimina un like de una publicación -> DELETE /api/likes
    @Transactional
    @Override
    public MessageResponse unlikePost(InternalLikeRequest request) {
        //Obtenemos el usuario autenticado para impedir que se quiten likes en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario enviado en la request
        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("You do not have permission to remove likes as another user.");
        }

        //Buscamos el usuario que quiere quitar el like a partir del id recibido en la request
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getIdUsuario()
                ));

        //Buscamos la publicación de la que se quiere quitar el like
        Publicacion publicacion = publicacionRepository.findById(request.getIdPublicacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + request.getIdPublicacion()
                ));

        //Comprobamos que el like exista antes de eliminarlo
        if (!likeRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            throw new ResourceNotFoundException("Like not found for this user and post.");
        }

        //Eliminamos el like de base de datos
        likeRepository.deleteByUsuarioAndPublicacion(usuario, publicacion);

        //Construimos la respuesta de éxito
        MessageResponse response = new MessageResponse();
        response.setMensaje("Like removed successfully.");

        return response;
    }

    //Obtiene el número total de likes de una publicación -> GET /api/likes/post/{postId}
    @Override
    public InternalLikesCountResponse getPostLikesCount(Integer postId) {
        // Buscamos la publicación y lanzamos excepción 404 si no existe
        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId
                ));

        // Contamos los likes de la publicación
        long totalLikes = likeRepository.countByPublicacion(publicacion);

        // Construimos la respuesta
        InternalLikesCountResponse response = new InternalLikesCountResponse();
        response.setPostId(publicacion.getId());
        response.setTotalLikes((int) totalLikes);

        return response;
    }

    //Comprueba si un usuario ha dado like a una publicación -> GET /api/likes/check
    @Override
    public InternalLikeCheckResponse checkUserLike(Integer idUsuario, Integer idPublicacion) {
        //Obtenemos el usuario autenticado para impedir consultas en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario recibido
        if (!usuarioAutenticado.getId().equals(idUsuario)) {
            throw new UnauthorizedException("You do not have permission to check likes for another user.");
        }

        //Buscamos el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + idUsuario
                ));

        //Buscamos la publicación
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + idPublicacion
                ));

        //Comprobamos si existe el like
        boolean liked = likeRepository.existsByUsuarioAndPublicacion(usuario, publicacion);

        //Construimos la respuesta
        InternalLikeCheckResponse response = new InternalLikeCheckResponse();
        response.setLiked(liked);

        return response;
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