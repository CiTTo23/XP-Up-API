package com.david.xpup.backend.service;

import com.david.xpup.generated.model.AdminCommentResponse;
import com.david.xpup.generated.model.AdminCommentUpdateRequest;
import com.david.xpup.generated.model.AdminPagedCommentResponse;
import com.david.xpup.generated.model.MessageResponse;

public interface AdminCommentsService {

    AdminPagedCommentResponse getAdminComments(
            String query,
            Integer postId,
            Integer userId,
            Integer page,
            Integer size
    );

    AdminCommentResponse getAdminCommentById(Integer commentId);

    AdminCommentResponse updateAdminComment(Integer commentId, AdminCommentUpdateRequest request);

    MessageResponse deleteAdminComment(Integer commentId);
}