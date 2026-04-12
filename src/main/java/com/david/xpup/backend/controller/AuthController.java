package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.AuthService;
import com.david.xpup.generated.api.AuthApi;
import com.david.xpup.generated.model.AuthLoginRequest;
import com.david.xpup.generated.model.AuthLoginResponse;
import com.david.xpup.generated.model.AuthRegisterRequest;
import com.david.xpup.generated.model.AuthRegisterResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<AuthRegisterResponse> registerUser(AuthRegisterRequest authRegisterRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerUser(authRegisterRequest));
    }

    @Override
    public ResponseEntity<AuthLoginResponse> loginUser(AuthLoginRequest authLoginRequest) {
        return ResponseEntity.ok(authService.loginUser(authLoginRequest));
    }

    @Override
    public ResponseEntity<MessageResponse> logoutUser() {
        return ResponseEntity.ok(authService.logoutUser());
    }
}