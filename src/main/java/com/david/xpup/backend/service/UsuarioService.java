package com.david.xpup.backend.service;

import com.david.xpup.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final ExperienciaRepository experienciaRepository;
    private final LikeRepository likeRepository;
    private final GuardadoRepository guardadoRepository;
}