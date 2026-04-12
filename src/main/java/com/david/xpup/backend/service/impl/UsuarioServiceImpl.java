package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.PublicacionRepository;
import com.david.xpup.backend.repository.SeguimientoRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.service.UsuarioService;
import com.david.xpup.generated.model.InternalPostSummaryResponse;
import com.david.xpup.generated.model.InternalUserProfileResponse;
import com.david.xpup.generated.model.InternalUserSummaryResponse;
import com.david.xpup.generated.model.InternalUserUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            SeguimientoRepository seguimientoRepository,
            PublicacionRepository publicacionRepository,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public InternalUserProfileResponse getUserProfile(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        return buildUserProfileResponse(usuario);
    }

    @Override
    public InternalUserProfileResponse updateUserProfile(Integer userId, InternalUserUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        if (usuarioRepository.existsByNombreUsuarioAndIdNot(request.getNombreUsuario(), userId)) {
            throw new DuplicateResourceException("El nombre de usuario ya está en uso");
        }

        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setFotoPerfil(request.getFotoPerfil());
        usuario.setBiografia(request.getBiografia());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return buildUserProfileResponse(usuarioActualizado);
    }

    @Override
    public List<InternalPostSummaryResponse> getUserPosts(Integer userId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InternalPostSummaryResponse> getUserLikedPosts(Integer userId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InternalPostSummaryResponse> getUserSavedPosts(Integer userId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InternalUserSummaryResponse> getUserFollowing(Integer userId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<InternalUserSummaryResponse> getUserFollowers(Integer userId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }


    private InternalUserProfileResponse buildUserProfileResponse(Usuario usuario) {
        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        long totalSeguidores = seguimientoRepository.countBySeguido(usuario);
        long totalSeguidos = seguimientoRepository.countBySeguidor(usuario);
        long totalPublicaciones = publicacionRepository.countByUsuario(usuario);

        return usuarioMapper.toUserProfileResponse(
                usuario,
                experiencia,
                totalSeguidores,
                totalSeguidos,
                totalPublicaciones
        );
    }
}