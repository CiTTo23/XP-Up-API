/***********************************************************************************************************************
*   Repositorio JPA para la gestión de comentarios en la base de datos del proyecto                                    *
*                                                                                                                      *
*   Proporciona operaciones CRUD básicas heredadas de JpaRepository y métodos de consulta más específicos              *
*   para recuperar y contar comentarios en función del usuario o de la publicación asociada                            *
*                                                                                                                      *
*   Funcionalidades principales:                                                                                       *
*       - Obtener todos los comentarios de una publicación ordenados por fecha ascendente                              *
*       - Obtener todos los comentarios realizados por un usuario                                                      *
*       - Contar cuántos comentarios tiene una publicación                                                             *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.Comentario;
import com.david.xpup.backend.entity.Publicacion;
import com.david.xpup.backend.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByPublicacionOrderByFechaComentarioAsc(Publicacion publicacion);

    List<Comentario> findByUsuario(Usuario usuario);

    void deleteByPublicacion(Publicacion publicacion);

    long countByPublicacion(Publicacion publicacion);

    List<Comentario> findByPublicacionOrderByFechaComentarioDesc(Publicacion publicacion);

    @Query("""
            SELECT c
            FROM Comentario c
            WHERE (:query IS NULL
                OR LOWER(c.contenido) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:postId IS NULL OR c.publicacion.id = :postId)
              AND (:userId IS NULL OR c.usuario.id = :userId)
            """)
    Page<Comentario> searchAdminComments(
            @Param("query") String query,
            @Param("postId") Integer postId,
            @Param("userId") Integer userId,
            Pageable pageable
    );
}