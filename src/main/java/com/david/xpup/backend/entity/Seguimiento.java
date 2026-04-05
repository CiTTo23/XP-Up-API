package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seguimientos")
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

    @ManyToOne
    @JoinColumn(name = "id_seguidor", nullable = false)
    private Usuario seguidor;

    @ManyToOne
    @JoinColumn(name = "id_seguido", nullable = false)
    private Usuario seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;
}