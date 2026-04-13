/***********************************************************************************************************************
*   Repositorio JPA para la gestión de la experiencia de los usuarios en la base de datos del proyecto                 *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con la experiencia asociada a cada usuario                                                            *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener la experiencia de un usuario                                                                         *
*       - Comprobar si un usuario ya tiene experiencia registrada                                                      *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Experiencia;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienciaRepository extends JpaRepository<Experiencia, Integer> {

    Optional<Experiencia> findByUsuario(Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}
