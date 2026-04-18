/***********************************************************************************************************************
*   Implementación del Usuario Service del sistema XP-Up                                                               *
*                                                                                                                      *
*   Esta clase contiene la lógica asociada a la consulta y actualización de perfiles de usuario, así como              *
*   a la obtención de información relacionada con su actividad social dentro de la plataforma                          *
*                                                                                                                      *
*   Metodos principales:                                                                                               *
*       - Obtener el perfil completo de un usuario                                                                     *
*       - Actualizar los datos editables del perfil                                                                    *
*       - Obtener las publicaciones creadas por un usuario                                                             *
*       - Obtener las publicaciones a las que un usuario ha dado like                                                  *
*       - Obtener las publicaciones guardadas por un usuario                                                           *
*       - Obtener la lista de usuarios seguidos                                                                        *
*       - Obtener la lista de seguidores                                                                               *
*                                                                                                                      *
*   Para ello, coordina diferentes repositorios del sistema, añadiendo información procedente de varias tablas         *
*   (usuarios, experiencia, seguimientos, publicaciones, likes, comentarios y guardados)                               *
*                                                                                                                      *
*   Importante:                                                                                                        *
*       - Define las validaciones necesarias para construir una infraestructura de Usuarios sólida                     *
*       - Centraliza la construcción del perfil completo de los usuarios mediante un metodo privado reutilizable       *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.*;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.*;
import com.david.xpup.backend.service.UsuarioService;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.generated.model.InternalUserUpdateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//Capa intermedia entre controller y repository -> lógica de negocio
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PublicacionRepository publicacionRepository;
    private final LikeRepository likeRepository;
    private final ComentarioRepository comentarioRepository;
    private final GuardadoRepository guardadoRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            SeguimientoRepository seguimientoRepository,
            PublicacionRepository publicacionRepository,
            LikeRepository likeRepository,
            ComentarioRepository comentarioRepository,
            GuardadoRepository guardadoRepository,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.publicacionRepository = publicacionRepository;
        this.likeRepository = likeRepository;
        this.comentarioRepository = comentarioRepository;
        this.guardadoRepository = guardadoRepository;
        this.usuarioMapper = usuarioMapper;
    }


    //Obtiene el perfil completo de un usuario a partir de su id -> GET /api/users/{userId}
    @Override
    public InternalUserProfileResponse getUserProfile(Integer userId) {
        //Busca usuario y lanza excepcion 404 si no existe
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        return buildUserProfileResponse(usuario);
    }


    //Actualiza los datos que son editables del perfil de un usuario -> PUT /api/users/{userId}
    @Transactional
    @Override
    public InternalUserProfileResponse updateUserProfile(Integer userId, InternalUserUpdateRequest request) {
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        if (!usuarioAutenticado.getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permisos para editar este perfil");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        if (usuarioRepository.existsByNombreUsuarioAndIdNot(request.getNombreUsuario(), userId)) {
            throw new DuplicateResourceException("El nombre de usuario ya está en uso");
        }

        usuario.setNombreUsuario(request.getNombreUsuario());

        if (request.getFotoPerfil() != null) {
            usuario.setFotoPerfil(request.getFotoPerfil());
        }

        if (request.getBiografia() != null) {
            usuario.setBiografia(request.getBiografia());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return buildUserProfileResponse(usuarioActualizado);
    }


    //Devuelve las publicaciones creadas por un usuario, incluyendo el estado de interacción del usuario autenticado -> GET /api/users/{userId}/posts
    @Override
    public List<InternalPostSummaryResponse> getUserPosts(Integer userId) {
        //Buscamos usuario del que se quieren ver las publicaciones
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        //Obtenemos el usuario autenticado, necesario para calcular el estado real de like y guardado
        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Obtenemos la experiencia del usuario, necesaria para el resumen de autor de publicación con nivel
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(usuario, experiencia);

        //Obtenemos publicaciones del usuario
        List<Publicacion> publicaciones = publicacionRepository.findByUsuarioOrderByFechaPublicacionDesc(usuario);

        //Transformamos cada publicación en su DTO resumen con estadísticas básicas y el estado real de interacción del usuario autenticado
        return publicaciones.stream()
                .map(publicacion -> {
                    //Comprobamos si el usuario que ve las publicaciones en el frontend tiene la publicacion guardada o marcada con "me gusta"
                    boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
                    boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

                    return mapToPostSummary(publicacion, usuarioResumen, likedByUser, savedByUser);
                })
                .toList();
    }


    //Devuelve las publicaciones a las que un usuario ha dado like -> GET /api/users/{userId}/likes
    @Override
    public List<InternalPostSummaryResponse> getUserLikedPosts(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        List<Like> likes = likeRepository.findByUsuarioOrderByFechaLikeDesc(usuario);

        return likes.stream()
                .map(Like::getPublicacion)
                .map(publicacion -> {
                    Usuario autor = publicacion.getUsuario();
                    Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
                    InternalUserSummaryResponse usuarioResumen =
                            usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);
                    //Comprobamos si el usuario que ve las publicaciones en el frontend tiene la publicacion guardada o marcada con "me gusta"
                    boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
                    boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

                    return mapToPostSummary(publicacion, usuarioResumen, likedByUser, savedByUser);
                })
                .toList();
    }


    //Devuelve las publicaciones guardadas por un usuario -> GET /api/users/{userId}/saved
    @Override
    public List<InternalPostSummaryResponse> getUserSavedPosts(Integer userId) {
        //Buscamos el usuario del que se quieren obtener las publicaciones guardadas
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Usuario usuarioAutenticado = getAuthenticatedUsuario();

        //Un usuario solo podrá ver sus publicaciones guardadas, no las de los demás.
        if (!usuarioAutenticado.getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permisos para ver las publicaciones guardadas de este usuario");
        }

        List<Guardado> guardados = guardadoRepository.findByUsuarioOrderByFechaGuardadoDesc(usuario);

        return guardados.stream()
                .map(Guardado::getPublicacion)
                .map(publicacion -> {
                    Usuario autor = publicacion.getUsuario();
                    Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
                    InternalUserSummaryResponse usuarioResumen =
                            usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);
                    //Comprobamos si el usuario que ve las publicaciones en el frontend tiene la publicacion guardada o marcada con "me gusta"
                    boolean likedByUser = likeRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);
                    boolean savedByUser = guardadoRepository.existsByUsuarioAndPublicacion(usuarioAutenticado, publicacion);

                    return mapToPostSummary(publicacion, usuarioResumen, likedByUser, savedByUser);
                })
                .toList();
    }

    //Devuelve la lista de usuarios a los que sigue un usuario -> GET /api/users/{userId}/following
    @Override
    public List<InternalUserSummaryResponse> getUserFollowing(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Seguimiento> seguimientos = seguimientoRepository.findBySeguidorOrderByFechaSeguimientoDesc(usuario);

        return seguimientos.stream()
                .map(Seguimiento::getSeguido)
                .map(usuarioSeguido -> {
                    Experiencia experiencia = experienciaRepository.findByUsuario(usuarioSeguido).orElse(null);
                    return usuarioMapper.toUserSummaryResponse(usuarioSeguido, experiencia);
                })
                .toList();
    }


    //Devuelve la lista de seguidores de un usuario -> GET /api/users/{userId}/followers
    @Override
    public List<InternalUserSummaryResponse> getUserFollowers(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Seguimiento> seguimientos = seguimientoRepository.findBySeguidoOrderByFechaSeguimientoDesc(usuario);

        return seguimientos.stream()
                .map(Seguimiento::getSeguidor)
                .map(usuarioSeguidor -> {
                    Experiencia experiencia = experienciaRepository.findByUsuario(usuarioSeguidor).orElse(null);
                    return usuarioMapper.toUserSummaryResponse(usuarioSeguidor, experiencia);
                })
                .toList();
    }

    //Construye la respuesta completa del perfil del usuario incluyendo experiencia y estadísticas sociales.
    private InternalUserProfileResponse buildUserProfileResponse(Usuario usuario) {
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        long totalSeguidores = seguimientoRepository.countBySeguido(usuario);
        long totalSeguidos = seguimientoRepository.countBySeguidor(usuario);
        long totalPublicaciones = publicacionRepository.countByUsuario(usuario);

        return usuarioMapper.toUserProfileResponse(
                usuario,
                experiencia,
                totalSeguidores,
                totalSeguidos,
                totalPublicaciones
        );
    }

    //Construye el DTO resumen de una publicación añadiendo estadísticas básicas y el estado de interacción del usuario.
    private InternalPostSummaryResponse mapToPostSummary(
            Publicacion publicacion,
            InternalUserSummaryResponse usuarioResumen,
            boolean likedByUser,
            boolean savedByUser
    ) {
        long totalLikes = likeRepository.countByPublicacion(publicacion);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

        return usuarioMapper.toPostSummaryResponse(
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
            throw new UnauthorizedException("Usuario no autenticado");
        }

        String nombreUsuario = authentication.getName();

        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UnauthorizedException("Usuario autenticado no encontrado"));
    }
}