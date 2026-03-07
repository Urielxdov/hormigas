package com.example.hormigas.movimiento.entity;

import com.example.hormigas.empresa.entity.Empresa;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.usuario.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "movimiento",
        indexes = {
                @Index(name = "idx_mov_producto", columnList = "producto_id"),
                @Index(name = "idx_mov_sucursal", columnList = "sucursal_id"),
                @Index(name = "idx_mov_fecha", columnList = "fecha")
        }
)
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String tipoMovimiento;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    private String referencia;

    public Movimiento() {}
}
