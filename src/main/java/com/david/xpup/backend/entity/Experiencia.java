/***********************************************************************************************************************
*   Entidad JPA que representa la tabla experiencia de la base de datos del proyecto                                   *
*                                                                                                                      *
*   Ejemplo de experiencia en JSON:                                                                                    *
*       {                                                                                                              *
*           "id": 1,                                                                                                   *
*           "usuarioId": 5,                                                                                            *
*           "xpTotal": 1250,                                                                                           *
*           "nivel": 3                                                                                                 *
*       }                                                                                                              *
*   Representa la experiencia acumulada y el nivel actual de un usuario dentro del sistema                             *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
//creación de uniqueConstraint para evitar que existan dos filas de experiencia para el mismo id_usuario
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
    @JoinColumn(name = "id_usuario", nullable = false, unique = true) //FK Usuarios
    private Usuario usuario;

    @Column(name = "xp_total", nullable = false)
    private Integer xpTotal;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;
}