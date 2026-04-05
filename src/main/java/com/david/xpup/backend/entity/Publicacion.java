package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "publicaciones")
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

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
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