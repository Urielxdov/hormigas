package com.example.hormigas.producto.entity;

import com.example.hormigas.empresa.entity.Empresa;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "producto",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"empresa_id", "sku"})
        }
)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String sku;

    @Column(precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private boolean activo = true;

    public Producto() {}
}