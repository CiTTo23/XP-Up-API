/***********************************************************************************************************************
*   Repositorio JPA para la gestión de "me gusta" en la base de datos del proyecto                                     *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
*   relacionados con los likes que los usuarios han dado a las publicaciones                                           *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener los likes de una publicación                                                                         *
*       - Obtener los likes realizados por un usuario ordenados por fecha descendente                                  *
*       - Comprobar si un usuario ha dado like a una publicación                                                       *
*       - Eliminar un like concreto (usuario + publicación)                                                            *
*       - Contar cuántos likes tiene una publicación                                                                   *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Like;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Integer> {

    List<Like> findByPublicacion(Publicacion publicacion);

    List<Like> findByUsuarioOrderByFechaLikeDesc(Usuario usuario);

    Optional<Like> findByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    boolean existsByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    void deleteByUsuarioAndPublicacion(Usuario usuario, Publicacion publicacion);

    long countByPublicacion(Publicacion publicacion);

    List<Like> findByUsuario(Usuario usuario);

}