package com.example.hormigas.movimiento.entity;

import com.example.hormigas.motivo.entity.MotivoMovimiento;
import com.example.hormigas.producto.entity.Producto;
import com.example.hormigas.sucursal.entity.Sucursal;
import com.example.hormigas.security.domain.Usuario;
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
    @JoinColumn(
            name = "producto_id",
            foreignKey = @ForeignKey(name = "fk_movimiento_producto")
    )
    private Producto producto;

    @ManyToOne
    @JoinColumn(
            name = "sucursal_id",
            foreignKey = @ForeignKey(name = "fk_movimiento_sucursal")
    )
    private Sucursal sucursal;

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

    public Movimiento() {}

    public Long getId() {
        return id;
    }


    public Sucursal getSucursal() {
        return sucursal;
    }

    public Producto getProducto() {
        return producto;
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

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
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

//    public MotivoMovimiento getMotivo() {
//        return motivo;
//    }

//    public void setMotivo(MotivoMovimiento motivo) {
//        this.motivo = motivo;
//    }

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
}
