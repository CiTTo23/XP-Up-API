package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.LikeService;
import com.david.xpup.generated.api.LikesApi;
import com.david.xpup.generated.model.InternalLikeCheckResponse;
import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalLikesCountResponse;
import com.david.xpup.generated.model.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LikeController implements LikesApi {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Override
    public ResponseEntity<MessageResponse> likePost(@Valid InternalLikeRequest internalLikeRequest) {
        return ResponseEntity.ok(likeService.likePost(internalLikeRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> unlikePost(InternalLikeRequest internalLikeRequest) {
        return ResponseEntity.ok(likeService.unlikePost(internalLikeRequest));
    }

    @Override
    public ResponseEntity<InternalLikesCountResponse> getPostLikesCount(Integer postId) {
        return ResponseEntity.ok(likeService.getPostLikesCount(postId));
    }

    @Override
    public ResponseEntity<InternalLikeCheckResponse> checkUserLike(Integer idUsuario, Integer idPublicacion) {
        return ResponseEntity.ok(likeService.checkUserLike(idUsuario, idPublicacion));
    }
}