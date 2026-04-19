package com.david.xpup.backend.service;

import com.david.xpup.generated.model.ExternalGameResponse;

import java.util.List;

public interface VideojuegoService {

    List<ExternalGameResponse> searchGames(String query);
}