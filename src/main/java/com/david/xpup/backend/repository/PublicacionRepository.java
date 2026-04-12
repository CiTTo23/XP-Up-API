package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findByUsuarioOrderByFechaPublicacionDesc(Usuario usuario);

    Page<Publicacion> findAllByOrderByFechaPublicacionDesc(Pageable pageable);

    Page<Publicacion> findByNombreJuegoIgnoreCaseOrderByFechaPublicacionDesc(String nombreJuego, Pageable pageable);

    Page<Publicacion> findByUsuarioInOrderByFechaPublicacionDesc(List<Usuario> usuarios, Pageable pageable);

    Page<Publicacion> findByUsuarioInAndNombreJuegoIgnoreCaseOrderByFechaPublicacionDesc(
            List<Usuario> usuarios,
            String nombreJuego,
            Pageable pageable
    );
}