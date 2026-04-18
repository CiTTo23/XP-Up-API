/***********************************************************************************************************************
*   Entidad JPA que representa la tabla likes de la base de datos del proyecto                                         *
*                                                                                                                      *
*   Ejemplo de like en JSON:                                                                                           *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "usuarioId": 5,                                                                                            *
*           "publicacionId": 12,                                                                                       *
*           "fechaLike": "2026-04-13T19:25:10"                                                                         *
*       }                                                                                                              *
*   Representa un "me gusta" con el que un usuario ha marcado una publicación                                          *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//creación de uniqueConstraint para evitar que no haya dos filas con la misma combinación de id_usuario e id_publicacion, un usuario no puede dar like 2 veces a la misma publicación
//creación de indices para mejorar el rendimiento en consultas del tipo "cuantas veces se ha marcado esta publicacion con "me gusta"?"
@Table(name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_like_usuario_publicacion", columnNames = {"id_usuario", "id_publicacion"})
        },
        indexes = {
                @Index(name = "idx_like_usuario", columnList = "id_usuario"),
                @Index(name = "idx_like_publicacion", columnList = "id_publicacion")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_like")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false) //FK Usuarios
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion", nullable = false) //FK publicaciones
    private Publicacion publicacion;

    @Column(name = "fecha_like", nullable = false)
    private LocalDateTime fechaLike;
}