/***********************************************************************************************************************
 *   Implementación del servicio general del panel de administración                                                   *
 *                                                                                                                     *
 *   Esta clase contiene la lógica asociada al dashboard administrativo y a la consulta del histórico                  *
 *   de operaciones administrativas                                                                                    *
 *                                                                                                                     *
 *   Métodos principales:                                                                                              *
 *       - Obtener estadísticas globales del sistema                                                                   *
 *       - Obtener el histórico paginado de operaciones administrativas                                                *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.OperacionAdmin;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.AdminMapper;
import com.david.xpup.backend.repository.*;
import com.david.xpup.backend.service.AdminGeneralService;
import com.david.xpup.generated.model.AdminOperationResponse;
import com.david.xpup.generated.model.AdminPagedOperationResponse;
import com.david.xpup.generated.model.AdminStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminGeneralServiceImpl implements AdminGeneralService {

    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_SUPERADMIN = "SUPERADMIN";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final LikeRepository likeRepository;
    private final GuardadoRepository guardadoRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final OperacionAdminRepository operacionAdminRepository;
    private final AdminMapper adminMapper;

    public AdminGeneralServiceImpl(
            UsuarioRepository usuarioRepository,
            PublicacionRepository publicacionRepository,
            ComentarioRepository comentarioRepository,
            LikeRepository likeRepository,
            GuardadoRepository guardadoRepository,
            SeguimientoRepository seguimientoRepository,
            OperacionAdminRepository operacionAdminRepository,
            AdminMapper adminMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.likeRepository = likeRepository;
        this.guardadoRepository = guardadoRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.operacionAdminRepository = operacionAdminRepository;
        this.adminMapper = adminMapper;
    }

    //Obtiene las estadísticas generales del sistema -> GET /api/admin/stats
    @Transactional(readOnly = true)
    @Override
    public AdminStatsResponse getAdminStats() {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        AdminStatsResponse response = new AdminStatsResponse();

        response.setTotalUsuarios((int) usuarioRepository.count());
        response.setTotalPublicaciones((int) publicacionRepository.count());
        response.setTotalComentarios((int) comentarioRepository.count());
        response.setTotalLikes((int) likeRepository.count());
        response.setTotalGuardados((int) guardadoRepository.count());
        response.setTotalSeguimientos((int) seguimientoRepository.count());
        response.setTotalOperacionesAdmin((int) operacionAdminRepository.count());

        return response;
    }

    //Obtiene el histórico de operaciones administrativas -> GET /api/admin/operations
    @Transactional(readOnly = true)
    @Override
    public AdminPagedOperationResponse getAdminOperations(
            String tipoOperacion,
            String entidadAfectada,
            Integer adminId,
            Integer page,
            Integer size
    ) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        PageRequest pageRequest = PageRequest.of(
                resolvePage(page),
                resolveSize(size),
                Sort.by(Sort.Direction.DESC, "fechaOperacion")
        );

        Page<OperacionAdmin> operationsPage = operacionAdminRepository.searchAdminOperations(
                normalize(tipoOperacion),
                normalize(entidadAfectada),
                adminId,
                pageRequest
        );

        List<AdminOperationResponse> operations = operationsPage.getContent().stream()
                .map(adminMapper::toAdminOperationResponse)
                .toList();

        return adminMapper.toAdminPagedOperationResponse(operations, operationsPage);
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