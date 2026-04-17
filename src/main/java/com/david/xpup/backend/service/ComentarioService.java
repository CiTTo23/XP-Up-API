package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalCommentRequest;
import com.david.xpup.generated.model.InternalCommentResponse;

import java.util.List;

public interface ComentarioService {

    InternalCommentResponse createComment(InternalCommentRequest request);

    List<InternalCommentResponse> getCommentsByPost(Integer postId);
}