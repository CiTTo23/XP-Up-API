/***********************************************************************************************************************
*   Entidad JPA que representa la tabla guardados de la base de datos del proyecto                                     *
*                                                                                                                      *
*   Ejemplo de guardado en JSON:                                                                                       *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "usuarioId": 5,                                                                                            *
*           "publicacionId": 12,                                                                                       *
*           "fechaGuardado": "2026-04-13T19:10:45"                                                                     *
*       }                                                                                                              *
*   Representa una publicación guardada por un usuario para que pueda consultarla más tarde                            *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//creación de uniqueConstraint para evitar que no haya dos filas con la misma combinación de id_usuario e id_publicacion, usuario no puede guardar 2 veces la misma publicación
//creación de indices para mejorar el rendimiento en consultas del tipo "cuantas veces se ha guardado esta publicacion?"
@Table(name = "guardados",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_guardado_usuario_publicacion", columnNames = {"id_usuario", "id_publicacion"})
        },
        indexes = {
                @Index(name = "idx_guardado_usuario", columnList = "id_usuario"),
                @Index(name = "idx_guardado_publicacion", columnList = "id_publicacion")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guardado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guardado")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false) //FK Usuarios
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion", nullable = false) //FK publicaciones
    private Publicacion publicacion;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado;
}