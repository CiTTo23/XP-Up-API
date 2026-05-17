/***********************************************************************************************************************
 *   Repositorio JPA para la gestión de publicaciones en la base de datos del proyecto                                  *
 *                                                                                                                      *
 *   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta específicos                  *
 *   relacionados con la obtención y filtrado de publicaciones                                                          *
 *                                                                                                                      *
 *   Funcionalidades principales:                                                                                       *
 *       - Obtener las publicaciones de un usuario ordenadas por fecha descendente                                      *
 *       - Obtener el feed global paginado ordenado por fecha                                                           *
 *       - Filtrar publicaciones por nombre de juego de forma parcial                                                   *
 *       - Obtener publicaciones de una lista de usuarios                                                               *
 *       - Filtrar el feed de seguidos por nombre de juego de forma parcial                                             *
 *       - Contar cuántas publicaciones ha realizado un usuario                                                         *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findByUsuarioOrderByFechaPublicacionDesc(Usuario usuario);

    Page<Publicacion> findAllByOrderByFechaPublicacionDesc(Pageable pageable);

    Page<Publicacion> findByNombreJuegoContainingIgnoreCaseOrderByFechaPublicacionDesc(
            String nombreJuego,
            Pageable pageable
    );

    Page<Publicacion> findByUsuarioInOrderByFechaPublicacionDesc(
            List<Usuario> usuarios,
            Pageable pageable
    );

    Page<Publicacion> findByUsuarioInAndNombreJuegoContainingIgnoreCaseOrderByFechaPublicacionDesc(
            List<Usuario> usuarios,
            String nombreJuego,
            Pageable pageable
    );


    long countByUsuario(Usuario usuario);

    @Query("""
            SELECT p
            FROM Publicacion p
            WHERE (:query IS NULL
                OR LOWER(p.titulo) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.nombreJuego) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:userId IS NULL OR p.usuario.id = :userId)
            """)
    Page<Publicacion> searchAdminPosts(
            @Param("query") String query,
            @Param("userId") Integer userId,
            Pageable pageable
    );
}