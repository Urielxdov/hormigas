package com.example.hormigas.movimiento.entity;

import com.example.hormigas.inventario.entity.Inventario;
import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.security.domain.Usuario;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "movimiento",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mov_venta_client_inventario",
                        columnNames = {"venta_client_id", "inventario_id"}
                )
        },
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
    @JoinColumn(
            name = "inventario_id",
            foreignKey = @ForeignKey(name = "fk_movimiento_inventario")
    )
    private Inventario inventario;

    @ManyToOne
    @JoinColumn(
            name = "usuario_id",
            foreignKey = @ForeignKey(name = "usuario_id")
    )
    private Usuario usuario;

//    @ManyToOne
//    @JoinColumn(
//            name = "motivo_id",
//            foreignKey = @ForeignKey(name = "fk_movimiento_motivo")
//    )
//    private MotivoMovimiento motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "tipo_movimiento")
    private TipoMovimiento tipoMovimiento;

    private int cantidad;

    private int stockAnterior;

    private int stockNuevo;

    private String referencia;

    private LocalDateTime fecha;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** Id generado por el cliente (POS) para deduplicar reintentos de una misma venta. */
    @Column(name = "venta_client_id", length = 64)
    private String ventaClientId;

    public Movimiento() {}

    public Long getId() {
        return id;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(int stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getVentaClientId() {
        return ventaClientId;
    }

    public void setVentaClientId(String ventaClientId) {
        this.ventaClientId = ventaClientId;
    }
}
