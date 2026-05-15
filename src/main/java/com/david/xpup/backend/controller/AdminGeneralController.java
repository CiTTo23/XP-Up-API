package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.AdminGeneralService;
import com.david.xpup.generated.api.AdminGeneralApi;
import com.david.xpup.generated.model.AdminPagedOperationResponse;
import com.david.xpup.generated.model.AdminStatsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminGeneralController implements AdminGeneralApi {

    private final AdminGeneralService adminGeneralService;

    public AdminGeneralController(AdminGeneralService adminGeneralService) {
        this.adminGeneralService = adminGeneralService;
    }

    @Override
    public ResponseEntity<AdminStatsResponse> getAdminStats() {
        return ResponseEntity.ok(adminGeneralService.getAdminStats());
    }

    @Override
    public ResponseEntity<AdminPagedOperationResponse> getAdminOperations(
            String tipoOperacion,
            String entidadAfectada,
            Integer adminId,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(
                adminGeneralService.getAdminOperations(
                        tipoOperacion,
                        entidadAfectada,
                        adminId,
                        page,
                        size
                )
        );
    }
}