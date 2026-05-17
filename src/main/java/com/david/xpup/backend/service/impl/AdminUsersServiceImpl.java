/***********************************************************************************************************************
 *   Implementación del servicio de administración de usuarios                                                         *
 *                                                                                                                     *
 *   Esta clase contiene la lógica asociada a la consulta y gestión administrativa de usuarios                         *
 *   dentro del panel de administración de XP-Up                                                                        *
 *                                                                                                                     *
 *   Métodos principales:                                                                                              *
 *       - Obtener listado paginado de usuarios                                                                         *
 *       - Obtener detalle administrativo de un usuario                                                                 *
 *       - Actualizar datos de un usuario desde administración                                                         *
 *       - Eliminar un usuario desde administración                                                                     *
 *       - Modificar el rol de un usuario                                                                               *
 *                                                                                                                     *
 *   Importante:                                                                                                       *
 *       - Las operaciones críticas requieren rol SUPERADMIN                                                            *
 *       - Las operaciones de escritura quedan registradas en operaciones_admin                                         *
 *       - Se evita eliminar o degradar al último SUPERADMIN del sistema                                                *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.BadRequestException;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.AdminMapper;
import com.david.xpup.backend.repository.*;
import com.david.xpup.backend.service.AdminAuditService;
import com.david.xpup.backend.service.AdminUsersService;
import com.david.xpup.generated.model.AdminPagedUserResponse;
import com.david.xpup.generated.model.AdminUserResponse;
import com.david.xpup.generated.model.AdminUserRoleUpdateRequest;
import com.david.xpup.generated.model.AdminUserUpdateRequest;
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
public class AdminUsersServiceImpl implements AdminUsersService {

    private static final String ROL_USER = "USER";
    private static final String ROL_ADMIN = "ADMIN";
    private static final String ROL_SUPERADMIN = "SUPERADMIN";

    private static final String ENTIDAD_USUARIO = "USUARIO";

    private static final String OPERACION_ACTUALIZAR_USUARIO = "ACTUALIZAR_USUARIO";
    private static final String OPERACION_ELIMINAR_USUARIO = "ELIMINAR_USUARIO";
    private static final String OPERACION_CAMBIO_ROL = "CAMBIO_ROL";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final PublicacionRepository publicacionRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final AdminAuditService adminAuditService;
    private final LikeRepository likeRepository;
    private final GuardadoRepository guardadoRepository;
    private final ComentarioRepository comentarioRepository;
    private final OperacionAdminRepository operacionAdminRepository;
    private final AdminMapper adminMapper;

    public AdminUsersServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            PublicacionRepository publicacionRepository,
            SeguimientoRepository seguimientoRepository,
            LikeRepository likeRepository,
            GuardadoRepository guardadoRepository,
            ComentarioRepository comentarioRepository,
            OperacionAdminRepository operacionAdminRepository,
            AdminAuditService adminAuditService,
            AdminMapper adminMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.publicacionRepository = publicacionRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.likeRepository = likeRepository;
        this.guardadoRepository = guardadoRepository;
        this.comentarioRepository = comentarioRepository;
        this.operacionAdminRepository = operacionAdminRepository;
        this.adminAuditService = adminAuditService;
        this.adminMapper = adminMapper;
    }

    //Obtiene usuarios paginados para el panel de administración -> GET /api/admin/users
    @Transactional(readOnly = true)
    @Override
    public AdminPagedUserResponse getAdminUsers(String query, String rol, Integer page, Integer size) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        String rolNormalizado = normalize(rol);

        if (rolNormalizado != null && !isValidRole(rolNormalizado)) {
            throw new BadRequestException("Invalid role: " + rolNormalizado);
        }

        PageRequest pageRequest = PageRequest.of(
                resolvePage(page),
                resolveSize(size),
                Sort.by(Sort.Direction.DESC, "fechaRegistro")
        );

        Page<Usuario> usersPage = usuarioRepository.searchAdminUsers(
                normalize(query),
                rolNormalizado,
                pageRequest
        );

        List<AdminUserResponse> users = usersPage.getContent().stream()
                .map(this::buildAdminUserResponse)
                .toList();

        return adminMapper.toAdminPagedUserResponse(users, usersPage);
    }

    //Obtiene el detalle administrativo de un usuario -> GET /api/admin/users/{userId}
    @Transactional(readOnly = true)
    @Override
    public AdminUserResponse getAdminUserById(Integer userId) {
        Usuario admin = getAuthenticatedUsuario();
        requireAdmin(admin);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return buildAdminUserResponse(usuario);
    }

    //Actualiza los datos de un usuario desde administración -> PATCH /api/admin/users/{userId}
    @Transactional
    @Override
    public AdminUserResponse updateAdminUser(Integer userId, AdminUserUpdateRequest request) {
        Usuario admin = getAuthenticatedUsuario();
        requireSuperadmin(admin);

        validateAdminUserUpdateRequest(request);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getNombreUsuario() != null) {
            if (usuarioRepository.existsByNombreUsuarioAndIdNot(request.getNombreUsuario(), userId)) {
                throw new DuplicateResourceException("Username is already in use.");
            }

            usuario.setNombreUsuario(request.getNombreUsuario());
        }

        if (request.getEmail() != null) {
            if (usuarioRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new DuplicateResourceException("Email is already in use.");
            }

            usuario.setEmail(request.getEmail());
        }

        if (request.getFotoPerfil() != null) {
            usuario.setFotoPerfil(request.getFotoPerfil());
        }

        if (request.getBiografia() != null) {
            usuario.setBiografia(request.getBiografia());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ACTUALIZAR_USUARIO,
                ENTIDAD_USUARIO,
                usuarioActualizado.getId(),
                "Usuario actualizado desde el panel de administración: " + usuarioActualizado.getNombreUsuario()
        );

        return buildAdminUserResponse(usuarioActualizado);
    }

    //Elimina un usuario desde administración -> DELETE /api/admin/users/{userId}
    @Transactional
    @Override
    public MessageResponse deleteAdminUser(Integer userId) {
        Usuario admin = getAuthenticatedUsuario();
        requireSuperadmin(admin);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (admin.getId().equals(usuario.getId())) {
            throw new BadRequestException("A superadmin cannot delete himself.");
        }

        if (ROL_SUPERADMIN.equals(usuario.getRol()) && usuarioRepository.countByRol(ROL_SUPERADMIN) <= 1) {
            throw new BadRequestException("The last SUPERADMIN user cannot be deleted.");
        }

        String detalle = "Usuario eliminado desde el panel de administración: "
                + usuario.getNombreUsuario()
                + " (" + usuario.getEmail() + ")";

        List<Publicacion> publicacionesUsuario =
                publicacionRepository.findByUsuarioOrderByFechaPublicacionDesc(usuario);

        if (!publicacionesUsuario.isEmpty()) {
            likeRepository.deleteByPublicacionIn(publicacionesUsuario);
            guardadoRepository.deleteByPublicacionIn(publicacionesUsuario);
            comentarioRepository.deleteByPublicacionIn(publicacionesUsuario);
            publicacionRepository.deleteAll(publicacionesUsuario);
        }

        likeRepository.deleteByUsuario(usuario);
        guardadoRepository.deleteByUsuario(usuario);
        comentarioRepository.deleteByUsuario(usuario);
        seguimientoRepository.deleteBySeguidorOrSeguido(usuario, usuario);
        experienciaRepository.deleteByUsuario(usuario);

        operacionAdminRepository.desvincularAdminDeOperaciones(usuario);

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_ELIMINAR_USUARIO,
                ENTIDAD_USUARIO,
                usuario.getId(),
                detalle
        );

        usuarioRepository.delete(usuario);

        MessageResponse response = new MessageResponse();
        response.setMensaje("Usuario eliminado correctamente");
        return response;
    }

    //Modifica el rol de un usuario -> PATCH /api/admin/users/{userId}/role
    @Transactional
    @Override
    public AdminUserResponse updateAdminUserRole(Integer userId, AdminUserRoleUpdateRequest request) {
        Usuario admin = getAuthenticatedUsuario();
        requireSuperadmin(admin);

        if (request == null || request.getRol() == null) {
            throw new BadRequestException("Role is required.");
        }

        String nuevoRol = request.getRol().getValue();

        if (!isValidRole(nuevoRol)) {
            throw new BadRequestException("Invalid role: " + nuevoRol);
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String rolAnterior = usuario.getRol();

        if (admin.getId().equals(usuario.getId()) && ROL_SUPERADMIN.equals(rolAnterior) && !ROL_SUPERADMIN.equals(nuevoRol)) {
            throw new BadRequestException("A superadmin cannot remove his own SUPERADMIN role.");
        }

        if (ROL_SUPERADMIN.equals(rolAnterior)
                && !ROL_SUPERADMIN.equals(nuevoRol)
                && usuarioRepository.countByRol(ROL_SUPERADMIN) <= 1) {
            throw new BadRequestException("The system must have at least one SUPERADMIN user.");
        }

        usuario.setRol(nuevoRol);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        adminAuditService.registrarOperacion(
                admin,
                OPERACION_CAMBIO_ROL,
                ENTIDAD_USUARIO,
                usuarioActualizado.getId(),
                "Rol cambiado de " + rolAnterior + " a " + nuevoRol + " para el usuario " + usuarioActualizado.getNombreUsuario()
        );

        return buildAdminUserResponse(usuarioActualizado);
    }

    private AdminUserResponse buildAdminUserResponse(Usuario usuario) {
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        long totalPublicaciones = publicacionRepository.countByUsuario(usuario);
        long totalSeguidores = seguimientoRepository.countBySeguido(usuario);
        long totalSeguidos = seguimientoRepository.countBySeguidor(usuario);

        return adminMapper.toAdminUserResponse(
                usuario,
                experiencia,
                totalPublicaciones,
                totalSeguidores,
                totalSeguidos
        );
    }

    private void validateAdminUserUpdateRequest(AdminUserUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required.");
        }

        if (request.getNombreUsuario() == null
                && request.getEmail() == null
                && request.getFotoPerfil() == null
                && request.getBiografia() == null) {
            throw new BadRequestException("At least one field must be provided to update the user.");
        }
    }

    private void requireAdmin(Usuario usuario) {
        if (!ROL_ADMIN.equals(usuario.getRol()) && !ROL_SUPERADMIN.equals(usuario.getRol())) {
            throw new UnauthorizedException("You do not have permission to access the admin panel.");
        }
    }

    private void requireSuperadmin(Usuario usuario) {
        if (!ROL_SUPERADMIN.equals(usuario.getRol())) {
            throw new UnauthorizedException("You do not have permission to perform this superadmin operation.");
        }
    }

    private boolean isValidRole(String rol) {
        return ROL_USER.equals(rol) || ROL_ADMIN.equals(rol) || ROL_SUPERADMIN.equals(rol);
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