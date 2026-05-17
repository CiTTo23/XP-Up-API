/***********************************************************************************************************************
 *   Implementación del servicio de administración de comentarios                                                      *
 *                                                                                                                     *
 *   Esta clase contiene la lógica asociada a la consulta, edición y eliminación de comentarios                        *
 *   desde el panel de administración de XP-Up                                                                          *
 *                                                                                                                     *
 *   Métodos principales:                                                                                              *
 *       - Obtener listado paginado de comentarios                                                                      *
 *       - Obtener detalle administrativo de un comentario                                                              *
 *       - Actualizar el contenido de un comentario desde administración                                                *
 *       - Eliminar un comentario desde administración                                                                  *
 *                                                                                                                     *
 *   Importante:                                                                                                       *
 *       - Requiere rol ADMIN o SUPERADMIN                                                                              *
 *       - Las operaciones de escritura quedan registradas en operaciones_admin                                         *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Comentario;
import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.BadRequestException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.AdminMapper;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.ComentarioRepository;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.AdminAuditService;
import com.david.xpup.backend.service.AdminCommentsService;
import com.david.xpup.generated.model.AdminCommentResponse;
import com.david.xpup.generated.model.AdminCommentUpdateRequest;
import com.david.xpup.generated.model.AdminPagedCommentResponse;
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
public class AdminCommentsServiceImpl implements AdminCommentsService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_SUPERADMIN = "SUPERADMIN";

    private static final String ENTIDAD_COMENTARIO = "COMENTARIO";

    private static final String OPERACION_ACTUALIZAR_COMENTARIO = "ACTUALIZAR_COMENTARIO";
    private static final String OPERACION_ELIMINAR_COMENTARIO = "ELIMINAR_COMENTARIO";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final AdminAuditService adminAuditService;
    private final AdminMapper adminMapper;
    private final UsuarioMapper usuarioMapper;

    public AdminCommentsServiceImpl(
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ComentarioRepository comentarioRepository,
            ExperienciaRepository experienciaRepository,
            AdminAuditService adminAuditService,
            AdminMapper adminMapper,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.adminAuditService = adminAuditService;
        this.adminMapper = adminMapper;
        this.usuarioMapper = usuarioMapper;
    }

    //Obtiene comentarios paginados para el panel de administración -> GET /api/admin/comments
    @Transactional(readOnly = true)
    @Override
    public AdminPagedCommentResponse getAdminComments(
            String query,
            Integer postId,
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

        if (postId != null && !publicacionRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        if (userId != null && !usuarioRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        PageRequest pageRequest = PageRequest.of(
                resolvePage(page),
                resolveSize(size),
                Sort.by(Sort.Direction.DESC, "fechaComentario")
        );

        Page<Comentario> commentsPage = comentarioRepository.searchAdminComments(
                queryNormalizada,
                postId,
                userId,
                pageRequest
        );

        List<AdminCommentResponse> comments = commentsPage.getContent().stream()
                .map(this::buildAdminCommentResponse)
                .toList();

        return adminMapper.toAdminPagedCommentResponse(comments, commentsPage);
    }

    //Obtiene el detalle administrativo de un comentario -> GET /api/admin/comments/{commentId}
    @Transactional(readOnly = true)
    @Override
    public AdminCommentResponse getAdminCommentById(Integer commentId) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        Comentario comentario = comentarioRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        return buildAdminCommentResponse(comentario);
    }

    //Actualiza un comentario desde administración -> PATCH /api/admin/comments/{commentId}
    @Transactional
    @Override
    public AdminCommentResponse updateAdminComment(Integer commentId, AdminCommentUpdateRequest request) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        validateAdminCommentUpdateRequest(request);

        Comentario comentario = comentarioRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comentario.setContenido(request.getContenido());

        Comentario comentarioActualizado = comentarioRepository.save(comentario);

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ACTUALIZAR_COMENTARIO,
                ENTIDAD_COMENTARIO,
                comentarioActualizado.getId(),
                "Comentario actualizado desde el panel de administración. Publicación: "
                        + comentarioActualizado.getPublicacion().getTitulo()
                        + " | Autor: "
                        + comentarioActualizado.getUsuario().getNombreUsuario()
        );

        return buildAdminCommentResponse(comentarioActualizado);
    }

    //Elimina un comentario desde administración -> DELETE /api/admin/comments/{commentId}
    @Transactional
    @Override
    public MessageResponse deleteAdminComment(Integer commentId) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        Comentario comentario = comentarioRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        String detalle = "Comentario eliminado desde el panel de administración. Publicación: "
                + comentario.getPublicacion().getTitulo()
                + " | Autor: "
                + comentario.getUsuario().getNombreUsuario();

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ELIMINAR_COMENTARIO,
                ENTIDAD_COMENTARIO,
                comentario.getId(),
                detalle
        );

        comentarioRepository.delete(comentario);

        MessageResponse response = new MessageResponse();
        response.setMensaje("Comentario eliminado correctamente");
        return response;
    }

    private AdminCommentResponse buildAdminCommentResponse(Comentario comentario) {
        Usuario autor = comentario.getUsuario();
        Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);

        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(
                autor,
                experienciaAutor
        );

        return adminMapper.toAdminCommentResponse(comentario, usuarioResumen);
    }

    private void validateAdminCommentUpdateRequest(AdminCommentUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        if (request.getContenido() == null || request.getContenido().isBlank()) {
            throw new BadRequestException("Comment content is required.");
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