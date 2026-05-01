package com.david.xpup.backend.service.impl;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.exception.DuplicateResourceException;
import com.david.xpup.backend.exception.UnauthorizedException;
import com.david.xpup.backend.mapper.AuthMapper;
import com.david.xpup.backend.repository.ExperienciaRepository;
import com.david.xpup.backend.repository.UsuarioRepository;
import com.david.xpup.backend.security.JwtService;
import com.david.xpup.backend.service.AuthService;
import com.david.xpup.generated.model.AuthLoginRequest;
import com.david.xpup.generated.model.AuthLoginResponse;
import com.david.xpup.generated.model.AuthRegisterRequest;
import com.david.xpup.generated.model.AuthRegisterResponse;
import com.david.xpup.generated.model.MessageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            ExperienciaRepository experienciaRepository,
            PasswordEncoder passwordEncoder,
            AuthMapper authMapper,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.experienciaRepository = experienciaRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMapper = authMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    @Override
    public AuthRegisterResponse registerUser(AuthRegisterRequest request) {
        if (usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new DuplicateResourceException("Username is already in use.");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use.");
        }

        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fotoPerfil(null)
                .biografia(null)
                .fechaRegistro(LocalDateTime.now())
                .rol("USER")
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Experiencia experiencia = Experiencia.builder()
                .usuario(usuarioGuardado)
                .xpTotal(0)
                .nivel(1)
                .build();

        experienciaRepository.save(experiencia);

        return authMapper.toAuthRegisterResponse(usuarioGuardado);
    }

    @Override
    public AuthLoginResponse loginUser(AuthLoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailOrNombreUsuario(
                        request.getIdentificador(),
                        request.getIdentificador()
                )
                .orElseThrow(() -> new UnauthorizedException("Incorrect username/email or password."));
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new UnauthorizedException("Incorrect username/email or password.");
        }
        String token = jwtService.generateToken(usuario.getId());
        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setUser(authMapper.toAuthUserResponse(usuario));
        return response;
    }

    @Override
    public MessageResponse logoutUser() {
        MessageResponse response = new MessageResponse();
        response.setMensaje("Session closed successfully.");
        return response;
    }
}