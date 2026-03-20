package com.example.hormigas.security.domain;

import com.example.hormigas.security.domain.permiso.Permiso;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Futura implementacion de empresa

    @Column(nullable = false)
    private String nombre;

    // Tabla relacional
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private Set<Permiso> permiso = new HashSet<>();

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    public void setPermisos(Set<Permiso> permisos) {
        this.permiso = permisos;
    }

    public void addPermiso(Permiso permiso) {
        this.permiso.add(permiso);
    }

    public Rol() {}

    @Builder
    public Rol(String nombre, String descripcion, Boolean activo, Set<Permiso> permiso) {
        this.nombre = nombre;
        this.permiso = permiso;
        this.descripcion = descripcion;
        this.activo = activo;
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

    public Set<Permiso> getPermiso() {
        return permiso;
    }

    public void setPermiso(Set<Permiso> permiso) {
        this.permiso = permiso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}