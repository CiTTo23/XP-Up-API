/***********************************************************************************************************************
 *   Implementación del servicio de administración de publicaciones                                                    *
 *                                                                                                                     *
 *   Esta clase contiene la lógica asociada a la consulta, edición y eliminación de publicaciones                      *
 *   desde el panel de administración de XP-Up                                                                          *
 *                                                                                                                     *
 *   Métodos principales:                                                                                              *
 *       - Obtener listado paginado de publicaciones                                                                    *
 *       - Obtener detalle administrativo de una publicación                                                            *
 *       - Actualizar una publicación desde administración                                                              *
 *       - Eliminar una publicación desde administración                                                                *
 *                                                                                                                     *
 *   Importante:                                                                                                       *
 *       - Requiere rol ADMIN o SUPERADMIN                                                                              *
 *       - Las operaciones de escritura quedan registradas en operaciones_admin                                         *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.BadRequestException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.AdminMapper;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.ComentarioRepository;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.GuardadoRepository;
import com.david.xpup.backend.repository.LikeRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.AdminAuditService;
import com.david.xpup.backend.service.AdminPostsService;
import com.david.xpup.generated.model.AdminPagedPostResponse;
import com.david.xpup.generated.model.AdminPostResponse;
import com.david.xpup.generated.model.AdminPostUpdateRequest;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPostsServiceImpl implements AdminPostsService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_SUPERADMIN = "SUPERADMIN";

    private static final String ENTIDAD_PUBLICACION = "PUBLICACION";

    private static final String OPERACION_ACTUALIZAR_PUBLICACION = "ACTUALIZAR_PUBLICACION";
    private static final String OPERACION_ELIMINAR_PUBLICACION = "ELIMINAR_PUBLICACION";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ExperienciaRepository experienciaRepository;
    private final LikeRepository likeRepository;
    private final ComentarioRepository comentarioRepository;
    private final GuardadoRepository guardadoRepository;
    private final AdminAuditService adminAuditService;
    private final AdminMapper adminMapper;
    private final UsuarioMapper usuarioMapper;

    public AdminPostsServiceImpl(
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ExperienciaRepository experienciaRepository,
            LikeRepository likeRepository,
            ComentarioRepository comentarioRepository,
            GuardadoRepository guardadoRepository,
            AdminAuditService adminAuditService,
            AdminMapper adminMapper,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.experienciaRepository = experienciaRepository;
        this.likeRepository = likeRepository;
        this.comentarioRepository = comentarioRepository;
        this.guardadoRepository = guardadoRepository;
        this.adminAuditService = adminAuditService;
        this.adminMapper = adminMapper;
        this.usuarioMapper = usuarioMapper;
    }

    //Obtiene publicaciones paginadas para el panel de administración -> GET /api/admin/posts
    @Transactional(readOnly = true)
    @Override
    public AdminPagedPostResponse getAdminPosts(
            String query,
            Integer userId,
            Integer page,
            Integer size
    ) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        String queryNormalizada = normalize(query);

        if (queryNormalizada != null && queryNormalizada.length() < 2) {
            throw new BadRequestException("Query must have at least 2 characters.");
        }

        if (userId != null && !usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        PageRequest pageRequest = PageRequest.of(
                resolvePage(page),
                resolveSize(size),
                Sort.by(Sort.Direction.DESC, "fechaPublicacion")
        );

        Page<Publicacion> postsPage = publicacionRepository.searchAdminPosts(
                queryNormalizada,
                userId,
                pageRequest
        );

        List<AdminPostResponse> posts = postsPage.getContent().stream()
                .map(this::buildAdminPostResponse)
                .toList();

        return adminMapper.toAdminPagedPostResponse(posts, postsPage);
    }

    //Obtiene el detalle administrativo de una publicación -> GET /api/admin/posts/{postId}
    @Transactional(readOnly = true)
    @Override
    public AdminPostResponse getAdminPostById(Integer postId) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        return buildAdminPostResponse(publicacion);
    }

    //Actualiza una publicación desde administración -> PATCH /api/admin/posts/{postId}
    @Transactional
    @Override
    public AdminPostResponse updateAdminPost(Integer postId, AdminPostUpdateRequest request) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        validateAdminPostUpdateRequest(request);

        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (request.getTitulo() != null) {
            publicacion.setTitulo(request.getTitulo());
        }

        if (request.getDescripcion() != null) {
            publicacion.setDescripcion(request.getDescripcion());
        }

        if (request.getTipoContenido() != null) {
            publicacion.setTipoContenido(request.getTipoContenido().getValue());
        }

        if (request.getIdJuegoApi() != null) {
            publicacion.setIdJuegoApi(request.getIdJuegoApi());
        }

        if (request.getNombreJuego() != null) {
            publicacion.setNombreJuego(request.getNombreJuego());
        }

        if (request.getPortadaJuegoUrl() != null) {
            publicacion.setPortadaJuegoUrl(request.getPortadaJuegoUrl());
        }

        if (request.getArchivoUrl() != null) {
            publicacion.setArchivoUrl(request.getArchivoUrl());
        }

        if (request.getMiniaturaUrl() != null) {
            publicacion.setMiniaturaUrl(request.getMiniaturaUrl());
        }

        Publicacion publicacionActualizada = publicacionRepository.save(publicacion);

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ACTUALIZAR_PUBLICACION,
                ENTIDAD_PUBLICACION,
                publicacionActualizada.getId(),
                "Publicación actualizada desde el panel de administración: " + publicacionActualizada.getTitulo()
        );

        return buildAdminPostResponse(publicacionActualizada);
    }

    //Elimina una publicación desde administración -> DELETE /api/admin/posts/{postId}
    @Transactional
    @Override
    public MessageResponse deleteAdminPost(Integer postId) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        Publicacion publicacion = publicacionRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        String detalle = "Publicación eliminada desde el panel de administración: "
                + publicacion.getTitulo()
                + " | Autor: "
                + publicacion.getUsuario().getNombreUsuario();

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ELIMINAR_PUBLICACION,
                ENTIDAD_PUBLICACION,
                publicacion.getId(),
                detalle
        );

        publicacionRepository.delete(publicacion);

        MessageResponse response = new MessageResponse();
        response.setMensaje("Publicación eliminada correctamente");
        return response;
    }

    private AdminPostResponse buildAdminPostResponse(Publicacion publicacion) {
        Usuario autor = publicacion.getUsuario();
        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);

        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(
                autor,
                experienciaAutor
        );

        long totalLikes = likeRepository.countByPublicacion(publicacion);
        long totalComentarios = comentarioRepository.countByPublicacion(publicacion);
        long totalGuardados = guardadoRepository.countByPublicacion(publicacion);

        return adminMapper.toAdminPostResponse(
                publicacion,
                usuarioResumen,
                totalLikes,
                totalComentarios,
                totalGuardados
        );
    }

    private void validateAdminPostUpdateRequest(AdminPostUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        if (request.getTitulo() == null
                && request.getDescripcion() == null
                && request.getTipoContenido() == null
                && request.getIdJuegoApi() == null
                && request.getNombreJuego() == null
                && request.getPortadaJuegoUrl() == null
                && request.getArchivoUrl() == null
                && request.getMiniaturaUrl() == null) {
            throw new BadRequestException("At least one field must be provided to update the post.");
        }
    }

    private void requireAdmin(Usuario usuario) {
        if (!ROL_ADMIN.equals(usuario.getRol()) && !ROL_SUPERADMIN.equals(usuario.getRol())) {
            throw new UnauthorizedException("You do not have permission to access the admin panel.");
        }
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

    private int resolvePage(Integer page) {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }

    private int resolveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}