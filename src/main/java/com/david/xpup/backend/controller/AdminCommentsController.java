package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.AdminCommentsService;
import com.david.xpup.generated.api.AdminCommentsApi;
import com.david.xpup.generated.model.AdminCommentResponse;
import com.david.xpup.generated.model.AdminCommentUpdateRequest;
import com.david.xpup.generated.model.AdminPagedCommentResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCommentsController implements AdminCommentsApi {

    private final AdminCommentsService adminCommentsService;

    public AdminCommentsController(AdminCommentsService adminCommentsService) {
        this.adminCommentsService = adminCommentsService;
    }

    @Override
    public ResponseEntity<AdminPagedCommentResponse> getAdminComments(
            String query,
            Integer postId,
            Integer userId,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(
                adminCommentsService.getAdminComments(
                        query,
                        postId,
                        userId,
                        page,
                        size
                )
        );
    }

    @Override
    public ResponseEntity<AdminCommentResponse> getAdminCommentById(Integer commentId) {
        return ResponseEntity.ok(
                adminCommentsService.getAdminCommentById(commentId)
        );
    }

    @Override
    public ResponseEntity<AdminCommentResponse> updateAdminComment(
            Integer commentId,
            AdminCommentUpdateRequest adminCommentUpdateRequest
    ) {
        return ResponseEntity.ok(
                adminCommentsService.updateAdminComment(commentId, adminCommentUpdateRequest)
        );
    }

    @Override
    public ResponseEntity<MessageResponse> deleteAdminComment(Integer commentId) {
        return ResponseEntity.ok(
                adminCommentsService.deleteAdminComment(commentId)
        );
    }
}