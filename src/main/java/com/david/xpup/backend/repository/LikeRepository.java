package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Like;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

    List<Like> findByPublicacion(Publicacion publicacion);

    List<Like> findByUsuario(Usuario usuario);

    Optional<Like> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    long countByPublicacion(Publicacion publicacion);
}