package com.example.hormigas.usuario.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "rol_permiso")
@IdClass(RolPermisoId.class)
public class RolPermiso {
    @Id
    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    @Id
    @ManyToOne
    @JoinColumn(name = "permiso_id")
    private Permiso permiso;

    public RolPermiso () {}
}
