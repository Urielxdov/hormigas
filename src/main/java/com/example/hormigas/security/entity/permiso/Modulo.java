package com.example.hormigas.security.entity.permiso;

import jakarta.persistence.*;
import lombok.*;

/**
 * De momento solo abra Productos, Movimientos
 * Usuarios y sucursales
 */
@Entity
@Table(name = "modulo")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;


}
