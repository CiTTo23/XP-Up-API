package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Guardado;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuardadoRepository extends JpaRepository<Guardado, Integer> {

    List<Guardado> findByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);

    List<Guardado> findByPublicacion(Publicacion publicacion);

    Optional<Guardado> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    void deleteByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    long countByPublicacion(Publicacion publicacion);
}