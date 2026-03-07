package com.example.hormigas.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permiso")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    public Modulo() {}
}
