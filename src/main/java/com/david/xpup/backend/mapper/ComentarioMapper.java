package com.david.xpup.backend.mapper;

import com.david.xpup.generated.model.InternalCommentResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.backend.entity.Comentario;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class ComentarioMapper {

    public InternalCommentResponse toCommentResponse(
            Comentario comentario,
            InternalUserSummaryResponse usuarioResumen
    ) {
        InternalCommentResponse response = new InternalCommentResponse();
        response.setId(comentario.getId());
        response.setUsuario(usuarioResumen);
        response.setContenido(comentario.getContenido());
        response.setFechaComentario(comentario.getFechaComentario().atOffset(ZoneOffset.UTC));
        return response;
    }
}