package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experiencia", uniqueConstraints = {
        @UniqueConstraint(name = "uk_experiencia_usuario", columnNames = "id_usuario")
})
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "xp_total", nullable = false)
    private Integer xpTotal;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;
}