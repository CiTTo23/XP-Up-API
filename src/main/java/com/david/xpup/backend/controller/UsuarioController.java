package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.UsuarioService;
import com.david.xpup.generated.api.UsuariosApi;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.generated.model.InternalUserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioController implements UsuariosApi {

    private final UsuarioService usuarioService;


    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public ResponseEntity<InternalUserProfileResponse> getUserProfile(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserProfile(userId));
    }

    @Override
    public ResponseEntity<InternalUserProfileResponse> updateUserProfile(Integer userId, @Valid InternalUserUpdateRequest internalUserUpdateRequest) {
        return ResponseEntity.ok(usuarioService.updateUserProfile(userId, internalUserUpdateRequest));
    }

    @Override
    public ResponseEntity<List<InternalPostSummaryResponse>> getUserPosts(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserPosts(userId));
    }

    @Override
    public ResponseEntity<List<InternalPostSummaryResponse>> getUserLikedPosts(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserLikedPosts(userId));
    }

    @Override
    public ResponseEntity<List<InternalPostSummaryResponse>> getUserSavedPosts(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserSavedPosts(userId));
    }

    @Override
    public ResponseEntity<List<InternalUserSummaryResponse>> getUserFollowing(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserFollowing(userId));
    }

    @Override
    public ResponseEntity<List<InternalUserSummaryResponse>> getUserFollowers(Integer userId) {
        return ResponseEntity.ok(usuarioService.getUserFollowers(userId));
    }
}