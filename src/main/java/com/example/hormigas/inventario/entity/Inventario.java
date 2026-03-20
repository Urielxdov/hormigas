package com.example.hormigas.inventario.entity;

import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventario",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventario_sucursal_producto",
                        columnNames = {"sucursal_id", "producto_id"}
                )
        },
        indexes = {
                @Index(name = "idx_inv_sucursal", columnList = "sucursal_id"),
                @Index(name = "idx_inv_producto", columnList = "producto_id")
        }
)
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "sucursal_id",
            foreignKey =  @ForeignKey(name = "fk_inventario_sucursal")
    )
    private Sucursal sucursal;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "producto_id",
            foreignKey = @ForeignKey(name = "fk_inventario_producto")
    )
    private Producto producto;

    private int stockMaximo;

    private int stockActual;

    private Integer stockMinimo;

    private LocalDateTime ultimaActualizacion;

    public Inventario() {}

    public Long getId() {
        return id;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getStockActual() {
        return stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public int getStockMaximo() {
        return stockMaximo;
    }

    public void setStockMaximo(int stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
