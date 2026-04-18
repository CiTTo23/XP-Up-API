/***********************************************************************************************************************
*   Implementación del Seguimiento Service del sistema XP-Up                                                           *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la gestión de seguimientos entre usuarios dentro de la plataforma         *
*                                                                                                                      *
*   Metodos principales:                                                                                               *
*       - Seguir a otro usuario                                                                                         *
*       - Dejar de seguir a un usuario                                                                                  *
*       - Comprobar si un usuario sigue a otro                                                                          *
*       - Obtener estadísticas de seguidores y seguidos                                                                 *
*                                                                                                                      *
*   Para ello, coordina los repositorios del sistema y aplica validaciones de existencia, permisos y duplicidad       *
*   antes de ejecutar operaciones sobre los seguimientos                                                               *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - Define las validaciones necesarias para construir una infraestructura de Seguimientos sólida                 *
*       - Centraliza la obtención del usuario autenticado mediante un metodo privado reutilizable                      *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Seguimiento;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.BadRequestException;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.SeguimientoMapper;
import com.david.xpup.backend.repository.SeguimientoRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.SeguimientoService;
import com.david.xpup.generated.model.InternalFollowCheckResponse;
import com.david.xpup.generated.model.InternalFollowRequest;
import com.david.xpup.generated.model.InternalFollowStatsResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//Capa intermedia entre controller y repository -> lógica de negocio
@Service
public class SeguimientoServiceImpl implements SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SeguimientoMapper seguimientoMapper;

    public SeguimientoServiceImpl(
            SeguimientoRepository seguimientoRepository,
            UsuarioRepository usuarioRepository,
            SeguimientoMapper seguimientoMapper
    ) {
        this.seguimientoRepository = seguimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.seguimientoMapper = seguimientoMapper;
    }


    //Crea una nueva relación de seguimiento entre dos usuarios -> POST /api/follows
    @Transactional
    @Override
    public MessageResponse followUser(InternalFollowRequest request) {
        //Obtenemos el usuario autenticado actual
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Un usuario solo puede seguir en su propio nombre
        if (!usuarioAutenticado.getId().equals(request.getIdSeguidor())) {
            throw new UnauthorizedException("No tienes permisos para seguir usuarios en nombre de otro");
        }

        //Un usuario no puede seguirse a sí mismo
        if (request.getIdSeguidor().equals(request.getIdSeguido())) {
            throw new BadRequestException("Un usuario no puede seguirse a sí mismo");
        }

        //Buscamos al usuario que realiza el seguimiento
        Usuario seguidor = usuarioRepository.findById(request.getIdSeguidor())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado con id: " + request.getIdSeguidor()));

        //Buscamos al usuario que va a ser seguido
        Usuario seguido = usuarioRepository.findById(request.getIdSeguido())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguido no encontrado con id: " + request.getIdSeguido()));

        //Comprobamos que no exista ya esa relación de seguimiento
        if (seguimientoRepository.existsBySeguidorAndSeguido(seguidor, seguido)) {
            throw new DuplicateResourceException("Ya sigues a este usuario");
        }

        //Creamos y guardamos el nuevo seguimiento
        Seguimiento seguimiento = Seguimiento.builder()
                .seguidor(seguidor)
                .seguido(seguido)
                .fechaSeguimiento(LocalDateTime.now())
                .build();

        seguimientoRepository.save(seguimiento);

        return seguimientoMapper.toMessageResponse("Usuario seguido correctamente");
    }


    //Elimina una relación de seguimiento entre dos usuarios -> DELETE /api/follows
    @Transactional
    @Override
    public MessageResponse unfollowUser(InternalFollowRequest request) {
        //Obtenemos el usuario autenticado actual
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Un usuario solo puede dejar de seguir en su propio nombre
        if (!usuarioAutenticado.getId().equals(request.getIdSeguidor())) {
            throw new UnauthorizedException("No tienes permisos para dejar de seguir usuarios en nombre de otro");
        }

        //Un usuario no puede dejar de seguirse a sí mismo
        if (request.getIdSeguidor().equals(request.getIdSeguido())) {
            throw new BadRequestException("Un usuario no puede dejar de seguirse a sí mismo");
        }

        //Buscamos al usuario que realiza la acción
        Usuario seguidor = usuarioRepository.findById(request.getIdSeguidor())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado con id: " + request.getIdSeguidor()));

        //Buscamos al usuario que se quiere dejar de seguir
        Usuario seguido = usuarioRepository.findById(request.getIdSeguido())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguido no encontrado con id: " + request.getIdSeguido()));

        //Eliminamos la relación de seguimiento si existe
        seguimientoRepository.findBySeguidorAndSeguido(seguidor, seguido)
                .ifPresent(seguimientoRepository::delete);

        return seguimientoMapper.toMessageResponse("Usuario dejado de seguir correctamente");
    }


    //Comprueba si un usuario sigue a otro -> GET /api/follows/check
    @Override
    public InternalFollowCheckResponse checkUserFollow(Integer idSeguidor, Integer idSeguido) {
        //Buscamos al usuario seguidor
        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguidor no encontrado con id: " + idSeguidor));

        //Buscamos al usuario seguido
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario seguido no encontrado con id: " + idSeguido));

        //Comprobamos si existe relación de seguimiento entre ambos usuarios
        boolean following = seguimientoRepository.existsBySeguidorAndSeguido(seguidor, seguido);

        return seguimientoMapper.toFollowCheckResponse(following);
    }


    //Obtiene las estadísticas de seguidores y seguidos de un usuario -> GET /api/follows/stats/{userId}
    @Override
    public InternalFollowStatsResponse getFollowStats(Integer userId) {
        //Buscamos el usuario del que se quieren obtener las estadísticas
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        //Contamos seguidores y seguidos del usuario
        long totalSeguidores = seguimientoRepository.countBySeguido(usuario);
        long totalSeguidos = seguimientoRepository.countBySeguidor(usuario);

        return seguimientoMapper.toFollowStatsResponse(totalSeguidores, totalSeguidos);
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