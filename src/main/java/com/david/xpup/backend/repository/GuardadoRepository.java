/***********************************************************************************************************************
*   Repositorio JPA para la gestión de publicaciones guardadas en la base de datos del proyecto                        *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con las publicaciones que los usuarios han guardado                                                   *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener las publicaciones guardadas por un usuario ordenadas por fecha descendente                           *
*       - Obtener los usuarios que han guardado una publicación                                                        *
*       - Comprobar si un usuario ha guardado una publicación                                                          *
*       - Eliminar un guardado concreto                                                                                *
*       - Contar cuántas veces ha sido guardada una publicación                                                        *
*                                                                                                                      *
***********************************************************************************************************************/

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

    List<Guardado> findByUsuario(Usuario usuario);


}