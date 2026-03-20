package com.example.hormigas.sucursal.entity;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.security.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sucursal")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "empresa_id",
            foreignKey = @ForeignKey(name = "fk_sucursal_empresa")
    )
    private Empresa empresa;

    @ManyToOne(optional = true)
    @JoinColumn(
            name = "encargado",
            foreignKey = @ForeignKey(name = "fk_sucursal_usuario")
    )
    private Usuario usuario;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    @Column(nullable = false)
    private boolean activa = true;

    public Sucursal() {}

    public Sucursal(Long id, Empresa empresa, Usuario usuario, String nombre, String direccion, boolean activa) {
        this.id = id;
        this.empresa = empresa;
        this.usuario = usuario;
        this.nombre = nombre;
        this.direccion = direccion;
        this.activa = activa;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
