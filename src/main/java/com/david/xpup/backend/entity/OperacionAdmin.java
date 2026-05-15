/***********************************************************************************************************************
 *   Entidad JPA que representa la tabla operaciones_admin de la base de datos del proyecto                            *
 *                                                                                                                     *
 *   Registra operaciones relevantes realizadas desde el panel de administración                                       *
 *                                                                                                                     *
 *   Ejemplo de operación administrativa en JSON:                                                                      *
 *       {                                                                                                             *
 *           "id": 1,                                                                                                  *
 *           "adminId": 3,                                                                                             *
 *           "nombreAdmin": "david_admin",                                                                             *
 *           "emailAdmin": "admin@xpup.com",                                                                           *
 *           "tipoOperacion": "ELIMINAR_PUBLICACION",                                                                  *
 *           "entidadAfectada": "PUBLICACION",                                                                         *
 *           "idEntidadAfectada": 25,                                                                                  *
 *           "detalle": "Publicación eliminada desde el panel de administración",                                     *
 *           "fechaOperacion": "2026-05-15T12:30:00"                                                                   *
 *       }                                                                                                             *
 *                                                                                                                     *
 ***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operaciones_admin", indexes = {
        @Index(name = "idx_operaciones_admin_admin", columnList = "id_admin"),
        @Index(name = "idx_operaciones_admin_tipo", columnList = "tipo_operacion"),
        @Index(name = "idx_operaciones_admin_entidad", columnList = "entidad_afectada"),
        @Index(name = "idx_operaciones_admin_fecha", columnList = "fecha_operacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacionAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_admin",
            foreignKey = @ForeignKey(name = "fk_operaciones_admin_usuario")
    )
    private Usuario admin;

    @Column(name = "nombre_admin", length = 20)
    private String nombreAdmin;

    @Column(name = "email_admin", length = 50)
    private String emailAdmin;

    @Column(name = "tipo_operacion", nullable = false, length = 50)
    private String tipoOperacion;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada")
    private Integer idEntidadAfectada;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDateTime fechaOperacion;
}