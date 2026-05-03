package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.PublicacionService;
import com.david.xpup.generated.api.PublicacionesApi;
import com.david.xpup.generated.model.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicacionController implements PublicacionesApi {

    private final PublicacionService publicacionService;

    public PublicacionController(PublicacionService publicacionService) {
        this.publicacionService = publicacionService;
    }

    @Override
    public ResponseEntity<InternalPostCreateResponse> createPost(@Valid InternalPostRequest internalPostRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicacionService.createPost(internalPostRequest));
    }

    @Override
    public ResponseEntity<InternalPostDetailResponse> getPostById(Integer postId) {
        return ResponseEntity.ok(publicacionService.getPostById(postId));
    }

    @Override
    public ResponseEntity<InternalPostDetailResponse> updatePost(
            Integer postId,
            InternalPostUpdateRequest internalPostUpdateRequest
    ) {
        return ResponseEntity.ok(publicacionService.updatePost(postId, internalPostUpdateRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> deletePost(Integer postId) {
        return ResponseEntity.ok(publicacionService.deletePost(postId));
    }
}