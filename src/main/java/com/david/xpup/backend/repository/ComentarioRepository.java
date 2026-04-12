package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Comentario;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByPublicacionOrderByFechaComentarioAsc(Publicacion publicacion);

    List<Comentario> findByUsuario(Usuario usuario);

    long countByPublicacion(Publicacion publicacion);
}