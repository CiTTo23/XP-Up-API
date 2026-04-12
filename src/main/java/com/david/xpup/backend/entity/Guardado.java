package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
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
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion", nullable = false)
    private Publicacion publicacion;

    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado;
}