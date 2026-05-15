/***********************************************************************************************************************
 *   Mapper encargado de transformar entidades del dominio a DTOs del módulo de administración                         *
 *                                                                                                                     *
 *   Responsabilidades principales:                                                                                    *
 *       - Convertir usuarios a DTOs administrativos                                                                   *
 *       - Convertir operaciones administrativas a DTOs del histórico                                                  *
 *       - Construir respuestas paginadas para el panel de administración                                              *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.mapper;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.OperacionAdmin;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.generated.model.AdminOperationResponse;
import com.david.xpup.generated.model.AdminPagedOperationResponse;
import com.david.xpup.generated.model.AdminPagedUserResponse;
import com.david.xpup.generated.model.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class AdminMapper {

    public AdminUserResponse toAdminUserResponse(
            Usuario usuario,
            Experiencia experiencia,
            long totalPublicaciones,
            long totalSeguidores,
            long totalSeguidos
    ) {
        AdminUserResponse response = new AdminUserResponse();

        response.setId(usuario.getId());
        response.setNombreUsuario(usuario.getNombreUsuario());
        response.setEmail(usuario.getEmail());
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setBiografia(usuario.getBiografia());

        response.setRol(AdminUserResponse.RolEnum.fromValue(usuario.getRol()));

        response.setFechaRegistro(usuario.getFechaRegistro().atOffset(ZoneOffset.UTC));
        response.setXpTotal(experiencia != null ? experiencia.getXpTotal() : 0);
        response.setNivel(experiencia != null ? experiencia.getNivel() : 1);

        response.setTotalPublicaciones((int) totalPublicaciones);
        response.setTotalSeguidores((int) totalSeguidores);
        response.setTotalSeguidos((int) totalSeguidos);

        return response;
    }

    public AdminPagedUserResponse toAdminPagedUserResponse(
            List<AdminUserResponse> users,
            Page<Usuario> page
    ) {
        AdminPagedUserResponse response = new AdminPagedUserResponse();

        response.setContent(users);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }

    public AdminOperationResponse toAdminOperationResponse(OperacionAdmin operacionAdmin) {
        AdminOperationResponse response = new AdminOperationResponse();

        response.setId(operacionAdmin.getId());

        if (operacionAdmin.getAdmin() != null) {
            response.setAdminId(operacionAdmin.getAdmin().getId());
        }

        response.setNombreAdmin(operacionAdmin.getNombreAdmin());
        response.setEmailAdmin(operacionAdmin.getEmailAdmin());
        response.setTipoOperacion(operacionAdmin.getTipoOperacion());
        response.setEntidadAfectada(
                AdminOperationResponse.EntidadAfectadaEnum.fromValue(operacionAdmin.getEntidadAfectada())
        );
        response.setIdEntidadAfectada(operacionAdmin.getIdEntidadAfectada());
        response.setDetalle(operacionAdmin.getDetalle());
        response.setFechaOperacion(operacionAdmin.getFechaOperacion().atOffset(ZoneOffset.UTC));

        return response;
    }

    public AdminPagedOperationResponse toAdminPagedOperationResponse(
            List<AdminOperationResponse> operations,
            Page<OperacionAdmin> page
    ) {
        AdminPagedOperationResponse response = new AdminPagedOperationResponse();

        response.setContent(operations);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }
}