package com.example.hormigas.security.entity.permiso;

import jakarta.persistence.*;

/**
 * De momento solo abra Productos, Movimientos
 * Usuarios y sucursales
 */
@Entity
@Table(name = "modulo")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    public Modulo() {}
}
