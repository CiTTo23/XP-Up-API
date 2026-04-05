package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienciaRepository extends JpaRepository<Experiencia, Integer> {

    Optional<Experiencia> findByUsuario(Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
