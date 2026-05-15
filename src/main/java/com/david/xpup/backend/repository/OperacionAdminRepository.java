/***********************************************************************************************************************
 *   Repositorio JPA para consultar y registrar operaciones administrativas                                            *
 ***********************************************************************************************************************/

package com.david.xpup.backend.repository;

import com.david.xpup.backend.entity.OperacionAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OperacionAdminRepository extends JpaRepository<OperacionAdmin, Integer> {

    @Query("""
            SELECT o
            FROM OperacionAdmin o
            WHERE (:tipoOperacion IS NULL OR o.tipoOperacion = :tipoOperacion)
              AND (:entidadAfectada IS NULL OR o.entidadAfectada = :entidadAfectada)
              AND (:adminId IS NULL OR o.admin.id = :adminId)
            """)
    Page<OperacionAdmin> searchAdminOperations(
            @Param("tipoOperacion") String tipoOperacion,
            @Param("entidadAfectada") String entidadAfectada,
            @Param("adminId") Integer adminId,
            Pageable pageable
    );
}