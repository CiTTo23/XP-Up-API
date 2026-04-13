/***********************************************************************************************************************
*   Repositorio JPA para la gestión de usuarios en la base de datos del proyecto                                       *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con la autenticación, registro y validación de usuarios                                               *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener un usuario por email                                                                                 *
*       - Obtener un usuario por nombre de usuario                                                                     *
*       - Obtener un usuario por email o nombre de usuario (login)                                                     *
*       - Comprobar si existe un email o nombre de usuario en el sistema                                               *
*       - Validar duplicados en operaciones de actualización de usuario                                                *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByEmailOrNombreUsuario(String email, String nombreUsuario);

    boolean existsByEmail(String email);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioAndIdNot(String nombreUsuario, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);
}