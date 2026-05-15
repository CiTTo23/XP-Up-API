package com.david.xpup.backend.service;

import com.david.xpup.backend.entity.Usuario;

public interface AdminAuditService {

    void registrarOperacion(
            Usuario admin,
            String tipoOperacion,
            String entidadAfectada,
            Integer idEntidadAfectada,
            String detalle
    );
}