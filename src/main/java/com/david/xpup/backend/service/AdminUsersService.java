package com.david.xpup.backend.service;

import com.david.xpup.generated.model.AdminPagedUserResponse;
import com.david.xpup.generated.model.AdminUserResponse;
import com.david.xpup.generated.model.AdminUserRoleUpdateRequest;
import com.david.xpup.generated.model.AdminUserUpdateRequest;
import com.david.xpup.generated.model.MessageResponse;

public interface AdminUsersService {

    AdminPagedUserResponse getAdminUsers(String query, String rol, Integer page, Integer size);

    AdminUserResponse getAdminUserById(Integer userId);

    AdminUserResponse updateAdminUser(Integer userId, AdminUserUpdateRequest request);

    MessageResponse deleteAdminUser(Integer userId);

    AdminUserResponse updateAdminUserRole(Integer userId, AdminUserRoleUpdateRequest request);
}