package com.example.hormigas.security.entity.permiso;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "permiso")
@Getter
@Setter
@Builder
@NoArgsConstructor // obligatorio para JPA
@AllArgsConstructor
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false)
    private String nombre;

}
