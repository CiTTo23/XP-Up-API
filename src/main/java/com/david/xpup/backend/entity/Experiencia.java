package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experiencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_experiencia")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "xp_total", nullable = false)
    private Integer xpTotal;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;
}