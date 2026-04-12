package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seguimientos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seguimiento_seguidor_seguido", columnNames = {"id_seguidor", "id_seguido"})
        },
        indexes = {
                @Index(name = "idx_seguimiento_seguidor", columnList = "id_seguidor"),
                @Index(name = "idx_seguimiento_seguido", columnList = "id_seguido")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguidor", nullable = false)
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguido", nullable = false)
    private Usuario seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;
}