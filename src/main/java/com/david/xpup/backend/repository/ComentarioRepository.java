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
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByPublicacionOrderByFechaComentarioAsc(Publicacion publicacion);

    List<Comentario> findByUsuario(Usuario usuario);

    long countByPublicacion(Publicacion publicacion);
}