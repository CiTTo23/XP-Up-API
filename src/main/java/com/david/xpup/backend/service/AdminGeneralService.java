package com.david.xpup.backend.service;

import com.david.xpup.generated.model.AdminPagedOperationResponse;
import com.david.xpup.generated.model.AdminStatsResponse;

public interface AdminGeneralService {

    AdminStatsResponse getAdminStats();

    AdminPagedOperationResponse getAdminOperations(
            String tipoOperacion,
            String entidadAfectada,
            Integer adminId,
            Integer page,
            Integer size
    );
}