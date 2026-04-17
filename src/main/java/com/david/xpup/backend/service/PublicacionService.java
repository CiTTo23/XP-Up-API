package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalPostCreateResponse;
import com.david.xpup.generated.model.InternalPostDetailResponse;
import com.david.xpup.generated.model.MessageResponse;
import com.david.xpup.generated.model.InternalPostRequest;

public interface PublicacionService {

    InternalPostCreateResponse createPost(InternalPostRequest request);

    InternalPostDetailResponse getPostById(Integer postId);

    MessageResponse deletePost(Integer postId);
}