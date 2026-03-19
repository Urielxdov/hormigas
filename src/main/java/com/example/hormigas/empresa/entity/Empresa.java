package com.example.hormigas.empresa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;


import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Builder
    public Empresa(String telefono, String nombre, String rfc, String direccion, boolean activo) {
        this.activo = activo;
        this.telefono = telefono;
        this.nombre = nombre;
        this.rfc = rfc;
        this.direccion = direccion;
    }

    @Builder
    public Empresa(String nombre, String rfc, String direccion, String telefono) {
        this.nombre = nombre;
        this.rfc = rfc;
        this.direccion = direccion;
        this.telefono = telefono;
    }
}
