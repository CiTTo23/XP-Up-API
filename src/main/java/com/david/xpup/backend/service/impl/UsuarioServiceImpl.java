package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.*;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.ResourceNotFoundException;
import com.david.xpup.backend.mapper.UsuarioMapper;
import com.david.xpup.backend.repository.*;
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
    private final LikeRepository likeRepository;
    private final ComentarioRepository comentarioRepository;
    private final GuardadoRepository guardadoRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            SeguimientoRepository seguimientoRepository,
            PublicacionRepository publicacionRepository,
            LikeRepository likeRepository,
            ComentarioRepository comentarioRepository,
            GuardadoRepository guardadoRepository,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.publicacionRepository = publicacionRepository;
        this.likeRepository = likeRepository;
        this.comentarioRepository = comentarioRepository;
        this.guardadoRepository = guardadoRepository;
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
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Experiencia experiencia = experienciaRepository.findByUsuario(usuario).orElse(null);
        InternalUserSummaryResponse usuarioResumen = usuarioMapper.toUserSummaryResponse(usuario, experiencia);

        List<Publicacion> publicaciones = publicacionRepository.findByUsuarioOrderByFechaPublicacionDesc(usuario);

        return publicaciones.stream()
                .map(publicacion -> {
                    long totalLikes = likeRepository.countByPublicacion(publicacion);
                    long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

                    return usuarioMapper.toPostSummaryResponse(
                            publicacion,
                            usuarioResumen,
                            totalLikes,
                            totalComentarios,
                            false,
                            false
                    );
                })
                .toList();
    }

    @Override
    public List<InternalPostSummaryResponse> getUserLikedPosts(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Like> likes = likeRepository.findByUsuarioOrderByFechaLikeDesc(usuario);

        return likes.stream()
                .map(Like::getPublicacion)
                .map(publicacion -> {
                    Usuario autor = publicacion.getUsuario();
                    Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
                    InternalUserSummaryResponse usuarioResumen =
                            usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

                    long totalLikes = likeRepository.countByPublicacion(publicacion);
                    long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

                    return usuarioMapper.toPostSummaryResponse(
                            publicacion,
                            usuarioResumen,
                            totalLikes,
                            totalComentarios,
                            true,
                            false
                    );
                })
                .toList();
    }

    @Override
    public List<InternalPostSummaryResponse> getUserSavedPosts(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Guardado> guardados = guardadoRepository.findByUsuarioOrderByFechaGuardadoDesc(usuario);

        return guardados.stream()
                .map(Guardado::getPublicacion)
                .map(publicacion -> {
                    Usuario autor = publicacion.getUsuario();
                    Experiencia experienciaAutor = experienciaRepository.findByUsuario(autor).orElse(null);
                    InternalUserSummaryResponse usuarioResumen =
                            usuarioMapper.toUserSummaryResponse(autor, experienciaAutor);

                    long totalLikes = likeRepository.countByPublicacion(publicacion);
                    long totalComentarios = comentarioRepository.countByPublicacion(publicacion);

                    return usuarioMapper.toPostSummaryResponse(
                            publicacion,
                            usuarioResumen,
                            totalLikes,
                            totalComentarios,
                            false,
                            true
                    );
                })
                .toList();
    }

    @Override
    public List<InternalUserSummaryResponse> getUserFollowing(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Seguimiento> seguimientos = seguimientoRepository.findBySeguidorOrderByFechaSeguimientoDesc(usuario);

        return seguimientos.stream()
                .map(Seguimiento::getSeguido)
                .map(usuarioSeguido -> {
                    Experiencia experiencia = experienciaRepository.findByUsuario(usuarioSeguido).orElse(null);
                    return usuarioMapper.toUserSummaryResponse(usuarioSeguido, experiencia);
                })
                .toList();
    }

    @Override
    public List<InternalUserSummaryResponse> getUserFollowers(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        List<Seguimiento> seguimientos = seguimientoRepository.findBySeguidoOrderByFechaSeguimientoDesc(usuario);

        return seguimientos.stream()
                .map(Seguimiento::getSeguidor)
                .map(usuarioSeguidor -> {
                    Experiencia experiencia = experienciaRepository.findByUsuario(usuarioSeguidor).orElse(null);
                    return usuarioMapper.toUserSummaryResponse(usuarioSeguidor, experiencia);
                })
                .toList();
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