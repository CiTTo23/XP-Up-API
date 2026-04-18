package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.GuardadoService;
import com.david.xpup.generated.api.GuardadosApi;
import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalSavedCheckResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GuardadoController implements GuardadosApi {

    private final GuardadoService guardadoService;

    public GuardadoController(GuardadoService guardadoService) {
        this.guardadoService = guardadoService;
    }

    @Override
    public ResponseEntity<MessageResponse> savePost(InternalLikeRequest internalLikeRequest) {
        return ResponseEntity.ok(guardadoService.savePost(internalLikeRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> unsavePost(InternalLikeRequest internalLikeRequest) {
        return ResponseEntity.ok(guardadoService.unsavePost(internalLikeRequest));
    }

    @Override
    public ResponseEntity<InternalSavedCheckResponse> checkUserSavedPost(Integer idUsuario, Integer idPublicacion) {
        return ResponseEntity.ok(guardadoService.checkUserSavedPost(idUsuario, idPublicacion));
    }
}