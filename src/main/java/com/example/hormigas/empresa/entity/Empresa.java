package com.example.hormigas.empresa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Entity
@Table(
        name = "empresa",
        indexes = {
                @Index(name = "idx_empresa_rfc", columnList = "rfc"),
                @Index(name = "idx_empresa_nombre", columnList = "nombre")
        }
)
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "rfc", length = 13,unique = true, nullable = false)
    private String rfc;

    private String direccion;

    @Column(length = 20, unique = true)
    private String telefono;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private boolean activo = true;

    public Empresa(String telefono, String nombre, String rfc, String direccion, boolean activo) {
        this.activo = activo;
        this.telefono = telefono;
        this.nombre = nombre;
        this.rfc = rfc;
        this.direccion = direccion;
    }

    public Empresa(String nombre, String rfc, String direccion, String telefono) {
        this.nombre = nombre;
        this.rfc = rfc;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public Empresa() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
