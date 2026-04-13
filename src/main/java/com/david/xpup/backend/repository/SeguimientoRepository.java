/***********************************************************************************************************************
*   Repositorio JPA para la gestión de seguimientos en la base de datos del proyecto                                   *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con las relaciones de seguimiento entre usuarios                                                      *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener a quién sigue un usuario (lista de seguidos) ordenados por fecha                                     *
*       - Obtener quién sigue a un usuario (lista de seguidores) ordenados por fecha                                   *
*       - Comprobar si un usuario sigue a otro                                                                         *
*       - Eliminar una relación de seguimiento (dejar de seguir)                                                       *
*       - Contar cuántos seguidores tiene un usuario                                                                   *
*       - Contar a cuántos usuarios sigue un usuario                                                                   *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Seguimiento;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    List<Seguimiento> findBySeguidorOrderByFechaSeguimientoDesc(Usuario seguidor);

    List<Seguimiento> findBySeguidoOrderByFechaSeguimientoDesc(Usuario seguido);

    Optional<Seguimiento> findBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    boolean existsBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    void deleteBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    long countBySeguido(Usuario seguido);

    long countBySeguidor(Usuario seguidor);
}