package com.example.hormigas.security.entity.rol;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.security.entity.permiso.Permiso;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @ManyToOne
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private Permiso permiso;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;
}