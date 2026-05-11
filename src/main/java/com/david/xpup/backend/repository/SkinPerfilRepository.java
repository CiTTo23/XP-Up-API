/***********************************************************************************************************************
*   Repositorio JPA para la gestión de skins de perfil en la base de datos del proyecto                               *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con el catálogo de skins desbloqueables del perfil de usuario                                        *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener una skin por su código                                                                               *
*       - Obtener una skin activa por su código                                                                        *
*       - Obtener una skin activa por su identificador                                                                 *
*       - Obtener todas las skins activas ordenadas por nivel requerido                                                *
*       - Comprobar si existe una skin por su código                                                                   *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.SkinPerfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkinPerfilRepository extends JpaRepository<SkinPerfil, Integer> {

    Optional<SkinPerfil> findByCodigo(String codigo);

    Optional<SkinPerfil> findByCodigoAndActivaTrue(String codigo);

    Optional<SkinPerfil> findByIdAndActivaTrue(Integer id);

    List<SkinPerfil> findByActivaTrueOrderByNivelRequeridoAsc();

    boolean existsByCodigo(String codigo);
}