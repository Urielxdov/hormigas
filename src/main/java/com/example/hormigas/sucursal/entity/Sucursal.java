package com.example.hormigas.sucursal.entity;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.security.entity.Usuario;
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

    @ManyToOne(optional = true)
    @JoinColumn(name = "encargado")
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    @Column(nullable = false)
    private boolean activa;

    public Sucursal() {}

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public boolean isActica() {
        return activa;
    }
}
