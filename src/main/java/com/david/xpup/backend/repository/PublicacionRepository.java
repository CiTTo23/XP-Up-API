package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findByUsuario(Usuario usuario);

    List<Publicacion> findAllByOrderByFechaPublicacionDesc();

    List<Publicacion> findByUsuarioOrderByFechaPublicacionDesc(Usuario usuario);
}