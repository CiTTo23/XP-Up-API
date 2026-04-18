/***********************************************************************************************************************
*   Entidad JPA que representa la tabla seguimientos de la base de datos del proyecto                                  *
*                                                                                                                      *
*   Ejemplo de seguimiento en JSON:                                                                                    *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "seguidorId": 5,                                                                                           *
*           "seguidoId": 8,                                                                                            *
*           "fechaSeguimiento": "2026-04-13T20:35:15"                                                                  *
*       }                                                                                                              *
*   Representa la relación en la que un usuario sigue a otro dentro de la plataforma                                   *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//creación de uniqueConstraint para evitar que haya dos filas con la misma combinación de id_seguidor e id_seguido
//creación de indices para mejorar el rendimiento en las consultas de seguidores y seguidos
@Table(name = "seguimientos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seguimiento", columnNames = {"id_seguidor", "id_seguido"})
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
    @JoinColumn(name = "id_seguidor", nullable = false) //FK Usuario seguidor
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seguido", nullable = false) //FK Usuario seguido
    private Usuario seguido;

    @Column(name = "fecha_seguimiento", nullable = false)
    private LocalDateTime fechaSeguimiento;
}