/***********************************************************************************************************************
*   Entidad JPA que representa la tabla publicaciones de la base de datos del proyecto                                 *
*                                                                                                                      *
*   Ejemplo de publicación en JSON:                                                                                    *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "usuarioId": 5,                                                                                            *
*           "titulo": "Mi mejor partida en Warzone",                                                                   *
*           "descripcion": "Clip épico consiguiendo victoria en solitario",                                            *
*           "tipoContenido": "VIDEO",                                                                                  *
*           "idJuegoApi": "12345",                                                                                     *
*           "nombreJuego": "Call of Duty: Warzone",                                                                    *
*           "portadaJuegoUrl": "https://example.com/portada.jpg",                                                      *
*           "archivoUrl": "https://example.com/video.mp4",                                                             *
*           "miniaturaUrl": "https://example.com/miniatura.jpg",                                                       *
*           "fechaPublicacion": "2026-04-13T20:10:30"                                                                  *
*       }                                                                                                              *
*   Representa una publicación creada por un usuario, incluyendo contenido multimedia y metadatos asociados            *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//indices creados para mejorar la velocidad de las consultas del tipo obtención de publicaciones por usuario, filtrado por juego y ordenación por fecha para el feed
@Table(name = "publicaciones", indexes = {
        @Index(name = "idx_publicacion_usuario", columnList = "id_usuario"),
        @Index(name = "idx_publicacion_nombre_juego", columnList = "nombre_juego"),
        @Index(name = "idx_publicacion_fecha", columnList = "fecha_publicacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false) //FK Usuarios
    private Usuario usuario;

    @Column(name = "titulo", nullable = false, length = 75)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tipo_contenido", nullable = false, length = 30)
    private String tipoContenido;

    @Column(name = "id_juego_api", length = 50)
    private String idJuegoApi;

    @Column(name = "nombre_juego", length = 50)
    private String nombreJuego;

    @Column(name = "portada_juego_url")
    private String portadaJuegoUrl;

    @Column(name = "archivo_url", nullable = false)
    private String archivoUrl;

    @Column(name = "miniatura_url")
    private String miniaturaUrl;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;
}