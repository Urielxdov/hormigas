package com.example.hormigas.security.domain.permiso;

import jakarta.persistence.*;
import lombok.*;

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

    public Modulo(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
