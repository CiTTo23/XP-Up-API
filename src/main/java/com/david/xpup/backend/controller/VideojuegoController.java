package com.david.xpup.backend.controller;

import com.david.xpup.backend.service.VideojuegoService;
import com.david.xpup.generated.api.VideojuegosApi;
import com.david.xpup.generated.model.ExternalGameResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VideojuegoController implements VideojuegosApi {

    private final VideojuegoService videojuegoService;

    public VideojuegoController(VideojuegoService videojuegoService) {
        this.videojuegoService = videojuegoService;
    }

    @Override
    public ResponseEntity<List<ExternalGameResponse>> searchGames(String query) {
        return ResponseEntity.ok(videojuegoService.searchGames(query));
    }
}