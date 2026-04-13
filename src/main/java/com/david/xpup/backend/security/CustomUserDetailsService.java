package com.david.xpup.backend.security;

import com.david.xpup.backend.entity.Usuario;
import com.david.xpup.backend.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new User(
                usuario.getNombreUsuario(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }

    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + userId));

        return new User(
                usuario.getNombreUsuario(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
        );
    }
}