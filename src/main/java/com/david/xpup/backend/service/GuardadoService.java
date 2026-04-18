package com.david.xpup.backend.service;

import com.david.xpup.generated.model.InternalLikeRequest;
import com.david.xpup.generated.model.InternalSavedCheckResponse;
import com.david.xpup.generated.model.MessageResponse;

public interface GuardadoService {

    MessageResponse savePost(InternalLikeRequest request);

    MessageResponse unsavePost(InternalLikeRequest request);

    InternalSavedCheckResponse checkUserSavedPost(Integer idUsuario, Integer idPublicacion);
}