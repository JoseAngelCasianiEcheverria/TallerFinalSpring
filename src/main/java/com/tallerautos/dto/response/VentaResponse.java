package com.tallerautos.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaResponse {

    private Long id;

    private Long clienteId;
    private String clienteNombre;
    private String clienteEmail;

    private Long vehiculoId;
    private String placa;
    private String marca;
    private String modelo;

    private LocalDateTime fechaVenta;
    private BigDecimal valorVehiculo;
    private BigDecimal descuentoAplicado;
    private BigDecimal total;
    private BigDecimal tasaCambio;

    
    private BigDecimal precioUsd;

    public VentaResponse() {
    }

    public VentaResponse(Long id, Long clienteId, String clienteNombre, String clienteEmail,
                         Long vehiculoId, String placa, String marca, String modelo,
                         LocalDateTime fechaVenta, BigDecimal valorVehiculo,
                         BigDecimal descuentoAplicado, BigDecimal total,
                         BigDecimal tasaCambio, BigDecimal precioUsd) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.vehiculoId = vehiculoId;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.fechaVenta = fechaVenta;
        this.valorVehiculo = valorVehiculo;
        this.descuentoAplicado = descuentoAplicado;
        this.total = total;
        this.tasaCambio = tasaCambio;
        this.precioUsd = precioUsd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
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
