/***********************************************************************************************************************
*   Entidad JPA que representa la tabla comentarios de la base de datos del proyecto                                   *
*                                                                                                                      *
*   Ejemplo de comentario en JSON:                                                                                     *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "usuarioId": 5,                                                                                            *
*           "publicacionId": 12,                                                                                       *
*           "contenido": "Buen vídeo, sigue así",                                                                      *
*           "fechaComentario": "2026-04-13T18:45:32"                                                                   *
*       }                                                                                                              *
*   Representa un comentario realizado por un usuario sobre una publicación                                            *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//creación de indices para mejorar el rendimiento en consultas del tipo "cuantas veces se ha comentado esta publicacion?"
@Table(name = "comentarios", indexes = {
        @Index(name = "idx_comentario_usuario", columnList = "id_usuario"),
        @Index(name = "idx_comentario_publicacion", columnList = "id_publicacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //identificador único autogenerado mediante auto-increment
    @Column(name = "id_comentario")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false) //FK usuarios
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion", nullable = false) //FK publicaciones
    private Publicacion publicacion;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_comentario", nullable = false)
    private LocalDateTime fechaComentario;
}