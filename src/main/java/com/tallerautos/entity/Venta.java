package com.tallerautos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    
    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    
    @Column(name = "valor_vehiculo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorVehiculo;

    
    @Column(name = "descuento_aplicado", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoAplicado;

    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    
    @Column(name = "tasa_cambio", precision = 12, scale = 4)
    private BigDecimal tasaCambio;

    
    @Column(name = "precio_usd", precision = 15, scale = 2)
    private BigDecimal precioUsd;

    public Venta() {
    }

    public Venta(Cliente cliente, Vehiculo vehiculo, LocalDateTime fechaVenta,
                 BigDecimal valorVehiculo, BigDecimal descuentoAplicado,
                 BigDecimal total, BigDecimal tasaCambio, BigDecimal precioUsd) {
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.fechaVenta = fechaVenta;
        this.valorVehiculo = valorVehiculo;
        this.descuentoAplicado = descuentoAplicado;
        this.total = total;
        this.tasaCambio = tasaCambio;
        this.precioUsd = precioUsd;
    }

    @PrePersist
    void asignarFechaVenta() {
        if (this.fechaVenta == null) {
            this.fechaVenta = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getValorVehiculo() {
        return valorVehiculo;
    }

    public void setValorVehiculo(BigDecimal valorVehiculo) {
        this.valorVehiculo = valorVehiculo;
    }

    public BigDecimal getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public void setDescuentoAplicado(BigDecimal descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTasaCambio() {
        return tasaCambio;
    }

    public void setTasaCambio(BigDecimal tasaCambio) {
        this.tasaCambio = tasaCambio;
    }

    public BigDecimal getPrecioUsd() {
        return precioUsd;
    }

    public void setPrecioUsd(BigDecimal precioUsd) {
        this.precioUsd = precioUsd;
    }
}
