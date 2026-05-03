package com.david.xpup.backend.service;

import com.david.xpup.generated.model.*;

public interface PublicacionService {

    InternalPostCreateResponse createPost(InternalPostRequest request);

    InternalPostDetailResponse getPostById(Integer postId);

    InternalPostDetailResponse updatePost(Integer postId, InternalPostUpdateRequest request);

    MessageResponse deletePost(Integer postId);
}