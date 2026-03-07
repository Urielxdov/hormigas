package com.example.hormigas.usuario.entity;

import java.io.Serializable;
import java.util.Objects;

public class RolPermisoId implements Serializable {

    private Long rol;
    private Long permiso;

    public RolPermisoId() {}

    public RolPermisoId (Long rol, Long permiso) {
        this.rol = rol;
        this.permiso = permiso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof RolPermisoId)) return false;
        RolPermisoId that = (RolPermisoId) o;
        return Objects.equals(rol, that.rol) &&
                Objects.equals(permiso, that.permiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rol, permiso);
    }
}
