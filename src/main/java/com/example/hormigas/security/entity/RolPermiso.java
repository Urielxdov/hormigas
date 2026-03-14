package com.example.hormigas.security.entity;

import com.example.hormigas.security.entity.permiso.Permiso;
import com.example.hormigas.security.entity.rol.Rol;
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
