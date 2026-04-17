/***********************************************************************************************************************
*   Implementación del Publicacion Service del sistema XP-Up                                                           *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la creación, consulta y eliminación de publicaciones dentro de la         *
*   plataforma                                                                                                         *
*                                                                                                                      *
*   Métodos principales:                                                                                               *
*       - Crear una nueva publicación                                                                                  *
*       - Obtener el detalle completo de una publicación                                                               *
*       - Eliminar una publicación                                                                                     *
*                                                                                                                      *
*   Para ello, coordina diferentes repositorios del sistema y aplica validaciones de existencia y permisos            *
*   antes de ejecutar operaciones sobre las publicaciones                                                              *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.PublicacionService;
import com.david.xpup.generated.model.InternalPostCreateResponse;
import com.david.xpup.generated.model.InternalPostDetailResponse;
import com.david.xpup.generated.model.InternalPostRequest;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public PublicacionServiceImpl(
            PublicacionRepository publicacionRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Crea una nueva publicación en el sistema -> POST /api/posts
    @Override
    public InternalPostCreateResponse createPost(InternalPostRequest request) {
        //Obtenemos el usuario autenticado para impedir que se creen publicaciones a nombre de otro usuario
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        if (!usuarioAutenticado.getId().equals(request.getIdUsuario())) {
            throw new UnauthorizedException("No tienes permisos para crear publicaciones para otro usuario");
        }

        //Buscamos el usuario autor de la publicación
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + request.getIdUsuario()
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

        //Construimos la respuesta de creación con el id generado
        InternalPostCreateResponse response = new InternalPostCreateResponse();
        response.setId(publicacionGuardada.getId());
        response.setMensaje("Publicación creada correctamente");

        return response;
    }

    @Override
    public InternalPostDetailResponse getPostById(Integer postId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public MessageResponse deletePost(Integer postId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
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