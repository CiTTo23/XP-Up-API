/***********************************************************************************************************************
 *   Implementación del servicio de auditoría administrativa                                                           *
 *                                                                                                                     *
 *   Registra automáticamente en base de datos las operaciones relevantes realizadas desde el panel                    *
 *   de administración                                                                                                *
 ***********************************************************************************************************************/

package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.OperacionAdmin;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.repository.OperacionAdminRepository;
import com.david.xpup.backend.service.AdminAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAuditServiceImpl implements AdminAuditService {

    private final OperacionAdminRepository operacionAdminRepository;

    public AdminAuditServiceImpl(OperacionAdminRepository operacionAdminRepository) {
        this.operacionAdminRepository = operacionAdminRepository;
    }

    @Transactional
    @Override
    public void registrarOperacion(
            Usuario admin,
            String tipoOperacion,
            String entidadAfectada,
            Integer idEntidadAfectada,
            String detalle
    ) {
        OperacionAdmin operacionAdmin = OperacionAdmin.builder()
                .admin(admin)
                .nombreAdmin(admin != null ? admin.getNombreUsuario() : null)
                .emailAdmin(admin != null ? admin.getEmail() : null)
                .tipoOperacion(tipoOperacion)
                .entidadAfectada(entidadAfectada)
                .idEntidadAfectada(idEntidadAfectada)
                .detalle(detalle)
                .fechaOperacion(LocalDateTime.now())
                .build();

        operacionAdminRepository.save(operacionAdmin);
    }
}