package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.SeguimientoService;
import com.david.xpup.generated.api.SeguimientosApi;
import com.david.xpup.generated.model.InternalFollowCheckResponse;
import com.david.xpup.generated.model.InternalFollowRequest;
import com.david.xpup.generated.model.InternalFollowStatsResponse;
import com.david.xpup.generated.model.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SeguimientoController implements SeguimientosApi {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @Override
    public ResponseEntity<MessageResponse> followUser(@Valid InternalFollowRequest internalFollowRequest) {
        return ResponseEntity.ok(seguimientoService.followUser(internalFollowRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> unfollowUser(@Valid InternalFollowRequest internalFollowRequest) {
        return ResponseEntity.ok(seguimientoService.unfollowUser(internalFollowRequest));
    }

    @Override
    public ResponseEntity<InternalFollowCheckResponse> checkUserFollow(Integer idSeguidor, Integer idSeguido) {
        return ResponseEntity.ok(seguimientoService.checkUserFollow(idSeguidor, idSeguido));
    }

    @Override
    public ResponseEntity<InternalFollowStatsResponse> getFollowStats(Integer userId) {
        return ResponseEntity.ok(seguimientoService.getFollowStats(userId));
    }
}