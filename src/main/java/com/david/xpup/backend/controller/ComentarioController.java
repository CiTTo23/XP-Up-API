package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.ComentarioService;
import com.david.xpup.generated.api.ComentariosApi;
import com.david.xpup.generated.model.InternalCommentRequest;
import com.david.xpup.generated.model.InternalCommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ComentarioController implements ComentariosApi {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @Override
    public ResponseEntity<InternalCommentResponse> createComment(InternalCommentRequest internalCommentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioService.createComment(internalCommentRequest));
    }

    @Override
    public ResponseEntity<List<InternalCommentResponse>> getCommentsByPost(Integer postId) {
        return ResponseEntity.ok(comentarioService.getCommentsByPost(postId));
    }
}