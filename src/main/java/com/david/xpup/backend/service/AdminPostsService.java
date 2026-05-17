package com.david.xpup.backend.service;

import com.david.xpup.generated.model.AdminPagedPostResponse;
import com.david.xpup.generated.model.AdminPostResponse;
import com.david.xpup.generated.model.AdminPostUpdateRequest;
import com.david.xpup.generated.model.MessageResponse;

public interface AdminPostsService {

    AdminPagedPostResponse getAdminPosts(
            String query,
            Integer userId,
            Integer page,
            Integer size
    );

    AdminPostResponse getAdminPostById(Integer postId);

    AdminPostResponse updateAdminPost(Integer postId, AdminPostUpdateRequest request);

    MessageResponse deleteAdminPost(Integer postId);
}