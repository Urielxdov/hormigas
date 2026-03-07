package com.example.hormigas.usuario.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usurio_rol")
@IdClass(UsuarioRolId.class)
public class UsuarioRol {
    @Id
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Id
    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public UsuarioRol() {}

}
