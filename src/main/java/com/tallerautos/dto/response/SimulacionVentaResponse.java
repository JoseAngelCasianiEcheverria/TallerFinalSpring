package com.tallerautos.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimulacionVentaResponse {

    private Long vehiculoId;
    private String placa;
    private String marca;
    private String modelo;
    private String estado;

    private BigDecimal valorVehiculo;
    private BigDecimal descuentoAplicado;
    private BigDecimal total;

    private BigDecimal tasaCambio;
    private BigDecimal precioUsd;

    
    private String explicacionDescuento;

    public SimulacionVentaResponse() {
    }

    public SimulacionVentaResponse(Long vehiculoId, String placa, String marca, String modelo,
                                   String estado, BigDecimal valorVehiculo,
                                   BigDecimal descuentoAplicado, BigDecimal total,
                                   BigDecimal tasaCambio, BigDecimal precioUsd,
                                   String explicacionDescuento) {
        this.vehiculoId = vehiculoId;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.estado = estado;
        this.valorVehiculo = valorVehiculo;
        this.descuentoAplicado = descuentoAplicado;
        this.total = total;
        this.tasaCambio = tasaCambio;
        this.precioUsd = precioUsd;
        this.explicacionDescuento = explicacionDescuento;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getExplicacionDescuento() {
        return explicacionDescuento;
    }

    public void setExplicacionDescuento(String explicacionDescuento) {
        this.explicacionDescuento = explicacionDescuento;
    }
}
