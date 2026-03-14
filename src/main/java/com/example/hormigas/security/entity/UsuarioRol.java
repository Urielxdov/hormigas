package com.example.hormigas.security.entity;

import com.example.hormigas.security.entity.rol.Rol;
import jakarta.persistence.*;

@Entity
@Table(name = "usurio_rol")
public class UsuarioRol {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Rol rol;

    public UsuarioRol() {}

}
