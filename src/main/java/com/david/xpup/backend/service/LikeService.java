package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalLikeCheckResponse;
import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalLikesCountResponse;
import com.david.xpup.generated.model.MessageResponse;

public interface LikeService {

    MessageResponse likePost(InternalLikeRequest request);

    MessageResponse unlikePost(InternalLikeRequest request);

    InternalLikesCountResponse getPostLikesCount(Integer postId);

    InternalLikeCheckResponse checkUserLike(Integer idUsuario, Integer idPublicacion);
}