package com.example.hormigas.inventario.entity;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import jakarta.persistence.*;

@Entity
@Table(
        name = "inventario",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sucursal_id, producto_id"})
        }
)
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int stockActual;

    private int stockMinimo;

}
