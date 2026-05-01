/***********************************************************************************************************************
 *   Implementación del Guardado Service del sistema XP-Up                                                              *
 *                                                                                                                      *
 *   Esta clase contiene la lógica asociada a la gestión de publicaciones guardadas dentro de la plataforma            *
 *                                                                                                                      *
 *   Métodos principales:                                                                                               *
 *       - Guardar una publicación                                                                                      *
 *       - Quitar una publicación guardada                                                                              *
 *       - Comprobar si una publicación está guardada por un usuario                                                    *
 *                                                                                                                      *
 *   Para ello, coordina diferentes repositorios del sistema y aplica validaciones de existencia, permisos y           *
 *   duplicidad antes de ejecutar operaciones sobre los guardados                                                       *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Guardado;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.repository.GuardadoRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.GuardadoService;
import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalSavedCheckResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GuardadoServiceImpl implements GuardadoService {

    private final GuardadoRepository guardadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;

    public GuardadoServiceImpl(
            GuardadoRepository guardadoRepository,
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository
    ) {
        this.guardadoRepository = guardadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
    }

    //Guarda una publicación -> POST /api/saved
    @Transactional
    @Override
    public MessageResponse savePost(InternalLikeRequest request) {
        //Obtenemos el usuario autenticado para impedir guardados en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario enviado en la request
        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("You do not have permission to save posts as another user.");
        }

        //Buscamos el usuario que realiza el guardado
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getIdUsuario()
                ));

        //Buscamos la publicación que se quiere guardar
        Publicacion publicacion = publicacionRepository.findById(request.getIdPublicacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + request.getIdPublicacion()
                ));

        //Comprobamos que no exista ya el guardado
        if (guardadoRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            throw new DuplicateResourceException("You have already saved this post.");
        }

        //Construimos la entidad Guardado a partir del usuario y la publicación
        Guardado guardado = Guardado.builder()
                .usuario(usuario)
                .publicacion(publicacion)
                .fechaGuardado(LocalDateTime.now())
                .build();

        //Guardamos el guardado en base de datos
        guardadoRepository.save(guardado);

        //Construimos la respuesta de éxito
        MessageResponse response = new MessageResponse();
        response.setMensaje("Post saved successfully.");

        return response;
    }

    //Elimina un guardado de una publicación -> DELETE /api/saved
    @Transactional
    @Override
    public MessageResponse unsavePost(InternalLikeRequest request) {
        //Obtenemos el usuario autenticado para impedir que se eliminen guardados en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario enviado en la request
        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("You do not have permission to unsave posts as another user.");
        }

        //Buscamos el usuario que quiere eliminar el guardado a partir del id recibido en la request
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getIdUsuario()
                ));

        //Buscamos la publicación de la que se quiere quitar el guardado
        Publicacion publicacion = publicacionRepository.findById(request.getIdPublicacion())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + request.getIdPublicacion()
                ));

        //Comprobamos que el guardado exista antes de eliminarlo
        if (!guardadoRepository.existsByUsuarioAndPublicacion(usuario, publicacion)) {
            throw new ResourceNotFoundException("Saved post not found for this user and post.");
        }

        //Eliminamos el guardado de base de datos
        guardadoRepository.deleteByUsuarioAndPublicacion(usuario, publicacion);

        //Construimos la respuesta de éxito
        MessageResponse response = new MessageResponse();
        response.setMensaje("Post unsaved successfully.");

        return response;
    }

    //Comprueba si un usuario ha guardado una publicación -> GET /api/saved/check
    @Override
    public InternalSavedCheckResponse checkUserSavedPost(Integer idUsuario, Integer idPublicacion) {
        //Obtenemos el usuario autenticado para impedir consultas en nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Comprobamos que el usuario autenticado coincide con el usuario recibido
        if (!usuarioAutenticado.getId().equals(idUsuario)) {
            throw new UnauthorizedException("You do not have permission to check saved posts for another user.");
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

        //Comprobamos si existe el guardado
        boolean saved = guardadoRepository.existsByUsuarioAndPublicacion(usuario, publicacion);

        //Construimos la respuesta
        InternalSavedCheckResponse response = new InternalSavedCheckResponse();
        response.setSaved(saved);

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