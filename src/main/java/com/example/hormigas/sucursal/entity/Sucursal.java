package com.example.hormigas.sucursal.entity;

import com.example.hormigas.empresa.entity.Empresa;
import jakarta.persistence.*;

@Entity
@Table(name = "sucursal")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    @Column(nullable = false)
    private boolean actica;

    public Sucursal() {}
}
