package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.AdminUsersService;
import com.david.xpup.generated.api.AdminUsersApi;
import com.david.xpup.generated.model.AdminPagedUserResponse;
import com.david.xpup.generated.model.AdminUserResponse;
import com.david.xpup.generated.model.AdminUserRoleUpdateRequest;
import com.david.xpup.generated.model.AdminUserUpdateRequest;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUsersController implements AdminUsersApi {

    private final AdminUsersService adminUsersService;

    public AdminUsersController(AdminUsersService adminUsersService) {
        this.adminUsersService = adminUsersService;
    }

    @Override
    public ResponseEntity<AdminPagedUserResponse> getAdminUsers(
            String query,
            String rol,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(
                adminUsersService.getAdminUsers(query, rol, page, size)
        );
    }

    @Override
    public ResponseEntity<AdminUserResponse> getAdminUserById(Integer userId) {
        return ResponseEntity.ok(
                adminUsersService.getAdminUserById(userId)
        );
    }

    @Override
    public ResponseEntity<AdminUserResponse> updateAdminUser(
            Integer userId,
            AdminUserUpdateRequest adminUserUpdateRequest
    ) {
        return ResponseEntity.ok(
                adminUsersService.updateAdminUser(userId, adminUserUpdateRequest)
        );
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAdminUser(Integer userId) {
        return ResponseEntity.ok(
                adminUsersService.deleteAdminUser(userId)
        );
    }

    @Override
    public ResponseEntity<AdminUserResponse> updateAdminUserRole(
            Integer userId,
            AdminUserRoleUpdateRequest adminUserRoleUpdateRequest
    ) {
        return ResponseEntity.ok(
                adminUsersService.updateAdminUserRole(userId, adminUserRoleUpdateRequest)
        );
    }
}