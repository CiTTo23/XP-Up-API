package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.AdminPostsService;
import com.david.xpup.generated.api.AdminPostsApi;
import com.david.xpup.generated.model.AdminPagedPostResponse;
import com.david.xpup.generated.model.AdminPostResponse;
import com.david.xpup.generated.model.AdminPostUpdateRequest;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminPostsController implements AdminPostsApi {

    private final AdminPostsService adminPostsService;

    public AdminPostsController(AdminPostsService adminPostsService) {
        this.adminPostsService = adminPostsService;
    }

    @Override
    public ResponseEntity<AdminPagedPostResponse> getAdminPosts(
            String query,
            Integer userId,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(
                adminPostsService.getAdminPosts(query, userId, page, size)
        );
    }

    @Override
    public ResponseEntity<AdminPostResponse> getAdminPostById(Integer postId) {
        return ResponseEntity.ok(
                adminPostsService.getAdminPostById(postId)
        );
    }

    @Override
    public ResponseEntity<AdminPostResponse> updateAdminPost(
            Integer postId,
            AdminPostUpdateRequest adminPostUpdateRequest
    ) {
        return ResponseEntity.ok(
                adminPostsService.updateAdminPost(postId, adminPostUpdateRequest)
        );
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAdminPost(Integer postId) {
        return ResponseEntity.ok(
                adminPostsService.deleteAdminPost(postId)
        );
    }
}