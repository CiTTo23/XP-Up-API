/***********************************************************************************************************************
 *   Entidad JPA que representa la tabla skins_perfil de la base de datos del proyecto                                  *
 *                                                                                                                      *
 *   Ejemplo de skin de perfil en JSON:                                                                                 *
 *       {                                                                                                              *
 *           "id": 2,                                                                                                   *
 *           "codigo": "BRONZE",                                                                                        *
 *           "nombre": "Bronce",                                                                                        *
 *           "descripcion": "Tema desbloqueable con detalles metálicos en bronce.",                                     *
 *           "nivelRequerido": 5,                                                                                       *
 *           "fondoUrlOscuro": "https://res.cloudinary.com/.../bronze-dark.png",                                        *
 *           "fondoUrlClaro": "https://res.cloudinary.com/.../bronze-light.png",                                        *
 *           "activa": true                                                                                             *
 *       }                                                                                                              *
 *   Representa una skin o fondo visual desbloqueable para el perfil de usuario dentro de XP-Up                         *
 *                                                                                                                      *
 ***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "skins_perfil", uniqueConstraints = {
        @UniqueConstraint(name = "uk_skin_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkinPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_skin")
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 30, unique = true)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "nivel_requerido", nullable = false)
    private Integer nivelRequerido;

    @Column(name = "fondo_url_oscuro", length = 255)
    private String fondoUrlOscuro;

    @Column(name = "fondo_url_claro", length = 255)
    private String fondoUrlClaro;

    @Column(name = "activa", nullable = false)
    private Boolean activa;
}