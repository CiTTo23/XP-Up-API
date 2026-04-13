/***********************************************************************************************************************
*   Entidad JPA que representa la tabla usuarios de la base de datos del proyecto                                      *
*                                                                                                                      *
*   Ejemplo de usuario en JSON:                                                                                        *
*       {                                                                                                              *
*           "id": 5,                                                                                                   *
*           "nombreUsuario": "david23",                                                                                *
*           "password": "$2a$10$abc123hashedpasswordexample", (password no se devuelve, solo ilustrativo)              *
*           "email": "david23@example.com",                                                                            *
*           "fotoPerfil": "https://example.com/foto.jpg",                                                              *
*           "biografia": "Apasionado de los videojuegos y creador de contenido ",                                      *
*           "fechaRegistro": "2026-04-01T12:00:00",                                                                    *
*           "rol": "USER"                                                                                              *
*       }                                                                                                              *
*   Representa a un usuario registrado en la plataforma con sus datos personales y de autenticación                    *
*                                                                                                                      *
***********************************************************************************************************************/

package com.david.xpup.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
//creación de uniqueConstraint para evitar dos usuarios con el mismo nombre de usuario o mismo email
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_nombre", columnNames = "nombre_usuario"),
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "nombre_usuario", nullable = false, length = 20, unique = true)
    private String nombreUsuario;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "rol", nullable = false, length = 30)
    private String rol;
}