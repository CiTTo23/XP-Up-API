package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Seguimiento;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    List<Seguimiento> findBySeguidor(Usuario seguidor);

    List<Seguimiento> findBySeguido(Usuario seguido);

    Optional<Seguimiento> findBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    boolean existsBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    void deleteBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    long countBySeguido(Usuario seguido);

    long countBySeguidor(Usuario seguidor);
}